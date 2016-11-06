/*
 * Copyright 2014 - 2016 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.AssertionFailure;
import org.hibernate.ScrollMode;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.StatementPreparer;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;

public class StatementPreparerImpl implements StatementPreparer {

    private JdbcCoordinator jdbcCoordinator;
    private SessionFactoryImplementor sessionFactoryImplementor;
    private boolean generated;
    private String[][] columns;
    private HibernateReturningResult<?> returningResult;

    public StatementPreparerImpl(JdbcCoordinator jdbcCoordinator, SessionFactoryImplementor sessionFactoryImplementor, boolean generated, String[][] columns, HibernateReturningResult<?> returningResult) {
        this.jdbcCoordinator = jdbcCoordinator;
        this.sessionFactoryImplementor = sessionFactoryImplementor;
        this.generated = generated;
        this.columns = columns;
        this.returningResult = returningResult;
    }

    protected final SessionFactoryOptions settings() {
        return sessionFactoryImplementor.getSessionFactoryOptions();
    }

    protected final Connection connection() {
        return logicalConnection().getPhysicalConnection();
    }

    protected final LogicalConnectionImplementor logicalConnection() {
        return jdbcCoordinator.getLogicalConnection();
    }

    protected final SqlExceptionHelper sqlExceptionHelper() {
        return getJdbcService().getSqlExceptionHelper();
    }

    @Override
    public Statement createStatement() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, final boolean isCallable) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    private void checkAutoGeneratedKeysSupportEnabled() {
        if (!settings().isGetGeneratedKeysEnabled()) {
            throw new AssertionFailure("getGeneratedKeys() support is not enabled");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, final int autoGeneratedKeys) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, final String[] columnNames) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public PreparedStatement prepareQueryStatement(String sql, final boolean isCallable, final ScrollMode scrollMode) {
        checkAutoGeneratedKeysSupportEnabled();
        jdbcCoordinator.executeBatch();
        PreparedStatement ps = new QueryStatementPreparationTemplate(sql) {

            public PreparedStatement doPrepare() throws SQLException {
                if (generated) {
                    return connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                } else {
                    String[] columnNames = new String[columns.length];
                    for (int i = 0; i < columns.length; i++) {
                        columnNames[i] = columns[i][0];
                    }
                    return connection().prepareStatement(sql, columnNames);
                }
            }
        }.prepareStatement();
        ps = (PreparedStatement) Proxy.newProxyInstance(ps.getClass().getClassLoader(), new Class[]{PreparedStatement.class}, new PreparedStatementInvocationHandler(ps, columns, returningResult));
        jdbcCoordinator.registerLastQuery(ps);
        return ps;
    }

    private abstract class StatementPreparationTemplate {

        protected final String sql;

        protected StatementPreparationTemplate(String incomingSql) {
            final String inspectedSql = jdbcCoordinator.getJdbcSessionOwner().getJdbcSessionContext().getStatementInspector().inspect(incomingSql);
            this.sql = inspectedSql == null ? incomingSql : inspectedSql;
        }

        public PreparedStatement prepareStatement() {
            try {
                getJdbcService().getSqlStatementLogger().logStatement(sql);

                final PreparedStatement preparedStatement;
                try {
                    jdbcCoordinator.getJdbcSessionOwner().getJdbcSessionContext().getObserver().jdbcPrepareStatementStart();
                    preparedStatement = doPrepare();
                    setStatementTimeout(preparedStatement);
                } finally {
                    jdbcCoordinator.getJdbcSessionOwner().getJdbcSessionContext().getObserver().jdbcPrepareStatementEnd();
                }
                postProcess(preparedStatement);
                return preparedStatement;
            } catch (SQLException e) {
                throw sqlExceptionHelper().convert(e, "could not prepare statement", sql);
            }
        }

        protected abstract PreparedStatement doPrepare() throws SQLException;

        public void postProcess(PreparedStatement preparedStatement) throws SQLException {
            jdbcCoordinator.getResourceRegistry().register(preparedStatement, true);
            //          logicalConnection().notifyObserversStatementPrepared();
        }

        private void setStatementTimeout(PreparedStatement preparedStatement) throws SQLException {
            final int remainingTransactionTimeOutPeriod = jdbcCoordinator.determineRemainingTransactionTimeOutPeriod();
            if (remainingTransactionTimeOutPeriod > 0) {
                preparedStatement.setQueryTimeout(remainingTransactionTimeOutPeriod);
            }
        }
    }

    private JdbcServices getJdbcService() {
        return jdbcCoordinator
                .getJdbcSessionOwner()
                .getJdbcSessionContext()
                .getServiceRegistry()
                .getService(JdbcServices.class);
    }

    private abstract class QueryStatementPreparationTemplate extends StatementPreparationTemplate {

        protected QueryStatementPreparationTemplate(String sql) {
            super(sql);
        }

        public void postProcess(PreparedStatement preparedStatement) throws SQLException {
            super.postProcess(preparedStatement);
            setStatementFetchSize(preparedStatement);
        }
    }

    private void setStatementFetchSize(PreparedStatement statement) throws SQLException {
        if (settings().getJdbcFetchSize() != null) {
            statement.setFetchSize(settings().getJdbcFetchSize());
        }
    }

}