package zone.glueck.sqlplot.sql;

import java.util.Map;
import java.util.Set;

/**
 * Created by zach on 10/23/16.
 */
public abstract class AbstractSqlGateway {

    /**
     * The {@link SQLController} that this gateway will query and listen for data update events.
     */
    protected final SQLController sqlController;

    /**
     * Sets the provided {@link SQLController} to the controller for this object. Adds itself to the property change
     * listener of the {@link SQLController}.
     *
     * @param sqlController the {@link SQLController} this gateway should use for queries and data updates
     */
    public AbstractSqlGateway(SQLController sqlController) {

        if (sqlController == null) {
            throw new IllegalArgumentException("sql controller cannot be null");
        }

        this.sqlController = sqlController;
        this.sqlController.addGatewayListener(this);

    }

    protected abstract void queryResultReturn(SQLData data);

    protected abstract void dataTableChange(Map<String, Set<String>> tablesAndFields);

}
