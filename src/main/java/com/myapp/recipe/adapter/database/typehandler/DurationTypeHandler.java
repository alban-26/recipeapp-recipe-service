package com.myapp.recipe.adapter.database.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

public class DurationTypeHandler extends BaseTypeHandler<Duration> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Duration parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, (int) parameter.toMinutes());  // store Duration as integer minutes
    }

    @Override
    public Duration getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int minutes = rs.getInt(columnName);
        return (minutes > 0) ? Duration.ofMinutes(minutes) : Duration.ZERO;
    }

    @Override
    public Duration getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int minutes = rs.getInt(columnIndex);
        return (minutes > 0) ? Duration.ofMinutes(minutes) : Duration.ZERO;
    }

    @Override
    public Duration getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        int minutes = cs.getInt(columnIndex);
        return (minutes > 0) ? Duration.ofMinutes(minutes) : Duration.ZERO;
    }
}
