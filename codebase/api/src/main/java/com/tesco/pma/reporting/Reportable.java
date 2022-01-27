package com.tesco.pma.reporting;

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
     * @return metadata of report.
     */
    ReportMetadata getReportMetadata();

    /**
     * Returns {@code true} if report data is available.
     *
     * @return {@code true} if report data is available
     */
    default boolean isAvailableReportData() {
        return true;
    }

    /**
     * Returns Report with data and metadata
     *
     * @return report.
     */
    default Report getReport() {
        var report = new Report();
        report.setData(isAvailableReportData() ? getReportData() : Collections.emptyList());
        report.setMetadata(getReportMetadata());
        return report;
    }
}
