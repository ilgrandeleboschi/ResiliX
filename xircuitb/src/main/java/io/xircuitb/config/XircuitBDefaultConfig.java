package io.xircuitb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("xb-def-template")
public class XircuitBDefaultConfig extends XircuitBYMLConfig {

}
