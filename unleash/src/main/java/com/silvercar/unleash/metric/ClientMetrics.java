package com.silvercar.unleash.metric;

import com.silvercar.unleash.event.UnleashEvent;
import com.silvercar.unleash.event.UnleashSubscriber;
import com.silvercar.unleash.util.UnleashConfig;

public class ClientMetrics implements UnleashEvent {

    private final String appName;
    private final String instanceId;
    private final MetricsBucket bucket;

    ClientMetrics(UnleashConfig config, MetricsBucket bucket) {
        this.appName = config.getAppName();
        this.instanceId = config.getInstanceId();
        this.bucket = bucket;
    }

    public String getAppName() {
        return appName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public MetricsBucket getBucket() {
        return bucket;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.clientMetrics(this);
    }

    @Override
    public String toString() {
        return "metrics:"
                + " appName=" + appName
                + " instanceId=" + instanceId
                ;
    }

}
