package smartthings.ratpack.protobuf;

/**
 * Cache (Caffeine) - specific configuration.
 */
public class CacheConfig {
    private Integer maximumSize = 1000;
    private Integer minutesTTL = 60;

    public Integer getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(Integer maximumSize) {
        this.maximumSize = maximumSize;
    }

    public Integer getMinutesTTL() {
        return minutesTTL;
    }

    public void setMinutesTTL(Integer minutesTTL) {
        this.minutesTTL = minutesTTL;
    }
}
