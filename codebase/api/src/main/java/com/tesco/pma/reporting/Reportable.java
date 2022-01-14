package com.tesco.pma.reporting;


import com.tesco.pma.reporting.metadata.ColumnMetadata;

import java.util.Collections;
import java.util.List;

/**
 * Interface for reportable entities.
 */
public interface Reportable {

    /**
     * Report data.
     *
     * @return list of lists with objects.
     */
    List<List<Object>> getReportData();

    /**
     * Report metadata.
     *
     * @return list of column's metadata.
     */
    List<ColumnMetadata> getReportMetadata();

    /**
     * Returns {@code true} if report data is available.
     *
     * @return {@code true} if report data is available
     */
    default boolean isAvailableReportData() {
        return true;
    }

    /**
     * Report.
     *
     * @return report.
     */
    default Report getReport() {
        var reportData = new Report();
        reportData.setData(isAvailableReportData() ? getReportData() : Collections.emptyList());
        reportData.setMetadata(getReportMetadata());
        return reportData;
    }
}
