package com.tesco.pma.api;

import java.util.Date;
import java.util.Objects;

// TODO revise class usage
public class RetryFilter {

    final Date currentDate;
    final int retryCount;
    final long retryInterval;
    final long retryOffSetInterval;

    public RetryFilter(int retryCount, long retryInterval, long retryOffSetInterval) {
        this(new Date(), retryCount, retryInterval, retryOffSetInterval);
    }

    public RetryFilter(Date currentDate, int retryCount, long retryInterval, long retryOffSetInterval) {
        Objects.requireNonNull(currentDate, "currentDate must be specified");
        this.currentDate = currentDate;
        if (retryCount <= 0) {
            throw new IllegalArgumentException("retryCount must be >0, but is " + retryCount);
        }
        this.retryCount = retryCount;
        if (retryInterval < 0) {
            throw new IllegalArgumentException("retryInterval must be positive, but is " + retryInterval);
        }
        this.retryInterval = retryInterval;
        if (retryOffSetInterval < 0) {
            throw new IllegalArgumentException("retryOffSetInterval must be positive, but is " + retryOffSetInterval);
        }
        this.retryOffSetInterval = retryOffSetInterval;
    }

    public Date getCurrentDate() {
        return new Date(currentDate.getTime());
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public long getRetryOffSetInterval() {
        return retryOffSetInterval;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentDate == null) ? 0 : currentDate.hashCode());
        result = prime * result + retryCount;
        result = prime * result + (int) (retryInterval ^ (retryInterval >>> 32));
        result = prime * result + (int) (retryOffSetInterval ^ (retryOffSetInterval >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RetryFilter other = (RetryFilter) obj;
        if (currentDate == null) {
            if (other.currentDate != null) {
                return false;
            }
        } else if (!currentDate.equals(other.currentDate)) {
            return false;
        }
        return retryCount == other.retryCount && retryInterval == other.retryInterval && retryOffSetInterval == other.retryOffSetInterval;
    }

    @Override
    public String toString() {
        return "RetryFilter {currentDate=" + currentDate + ", retryCount=" + retryCount + ", retryInterval=" + retryInterval
                + ", retryOffSetInterval=" + retryOffSetInterval + "}";
    }
}
