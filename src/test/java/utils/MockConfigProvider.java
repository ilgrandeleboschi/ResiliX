package utils;

import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.provider.XircuitBConfigProvider;
import org.springframework.stereotype.Component;

import static utils.MockBuilder.createXircuitBConfigModel;

@Component
public class MockConfigProvider implements XircuitBConfigProvider {
    @Override
    public XircuitBConfigModel apply() {
        return createXircuitBConfigModel();
    }
}
