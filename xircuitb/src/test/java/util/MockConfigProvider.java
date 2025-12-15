package util;

import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.provider.XircuitBConfigProvider;
import org.springframework.stereotype.Component;

import static util.XircuitBMockBuilder.createXircuitBConfigModel;

@Component
public class MockConfigProvider implements XircuitBConfigProvider {

    @Override
    public String name() {
        return "";
    }

    @Override
    public XircuitBConfigModel get() {
        return createXircuitBConfigModel();
    }

}
