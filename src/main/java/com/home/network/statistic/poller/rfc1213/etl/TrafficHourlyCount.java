package com.home.network.statistic.poller.rfc1213.etl;

import com.home.network.statistic.poller.rfc1213.out.IftableTrafficEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class TrafficHourlyCount {
    private final String date;
    private final Integer timeHourSecond;
    private final Long ifPhysAddress;
    private final String ifDescr;
    private long inBytes;
    private long outBytes;

    public TrafficHourlyCount(IftableTrafficEntity e) {
        this.date = e.obtainPollDate();
        this.timeHourSecond = e.obtainPollTimeHour();
        this.ifPhysAddress = e.getIfPhysAddress();
        this.ifDescr = e.getIfDescr();
        this.inBytes = 0;
        this.outBytes = 0;
    }

    public void adjustTraffic(IftableTrafficEntity old, IftableTrafficEntity nEW) {
        // always subtract prev state (because it is accumulated)
        this.inBytes += old.calcDiffRxOldNew(nEW);
        this.outBytes += old.calcDiffTxOldNew(nEW);
    }

    public static List<Object[]> obtainMappedRow(Map<TrafficHourlyCount, TrafficHourlyCount> map) {
        return map.values().stream().map(TrafficHourlyCount::obtainMappedRow).toList();
    }

    public Object[] obtainMappedRow() {
        return new Object[] {date, timeHourSecond, ifPhysAddress, ifDescr, inBytes + outBytes};
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TrafficHourlyCount that)) return false;
        return Objects.equals(date, that.date) && Objects.equals(timeHourSecond, that.timeHourSecond) && Objects.equals(ifPhysAddress, that.ifPhysAddress) && Objects.equals(ifDescr, that.ifDescr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, timeHourSecond, ifPhysAddress, ifDescr);
    }
}
