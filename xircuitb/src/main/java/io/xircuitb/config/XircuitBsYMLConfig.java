package io.xircuitb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties
public class XircuitBsYMLConfig {

    private Map<String, XircuitBYMLConfig> xircuitb = new HashMap<>();

}
