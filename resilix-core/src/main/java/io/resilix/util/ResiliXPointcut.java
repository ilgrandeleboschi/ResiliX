package io.resilix.util;

import org.aspectj.lang.annotation.Pointcut;

public class ResiliXPointcut {

    @Pointcut("@annotation(io.xircuitb.annotation.XircuitB)")
    public void xircuitB() {
    }

    @Pointcut("@annotation(io.xircuitb.annotation.XircuitBs)")
    public void xircuitBs() {
    }

    @Pointcut("xircuitB() || xircuitBs()")
    public void allResiliX() {
    }

}
