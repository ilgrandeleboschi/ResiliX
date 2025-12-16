package io.xircuitb.factory;

import io.resilix.factory.ResiliXNameFactory;
import io.resilix.model.ResiliXContext;
import io.xircuitb.annotation.XircuitB;
import org.springframework.stereotype.Component;

@Component
public class XircuitBNameFactory implements ResiliXNameFactory<XircuitB> {

    @Override
    public String resolveName(XircuitB xb, ResiliXContext ctx, int index) {
        if (!xb.name().isEmpty())
            return xb.name();

        String base = ctx.getMethod().getDeclaringClass().getSimpleName() + "." + ctx.getMethod().getName();
        String sigHash = Integer.toHexString(ctx.getMethod().toGenericString().hashCode());
        return base + "#" + sigHash + "_" + index;
    }

}
