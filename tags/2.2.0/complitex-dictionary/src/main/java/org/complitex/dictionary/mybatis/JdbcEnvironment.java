package org.complitex.dictionary.mybatis;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.10.13 13:40
 */
public class JdbcEnvironment {
    private String dataSource;
    private String environment;

    public JdbcEnvironment(String dataSource, String environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JdbcEnvironment that = (JdbcEnvironment) o;

        return !(dataSource != null ? !dataSource.equals(that.dataSource) : that.dataSource != null)
                && !(environment != null ? !environment.equals(that.environment) : that.environment != null);
    }

    @Override
    public int hashCode() {
        int result = dataSource != null ? dataSource.hashCode() : 0;
        result = 31 * result + (environment != null ? environment.hashCode() : 0);
        return result;
    }
}
