package com.holybell.rpcfx.demo.provider.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.holybell.rpcfx.demo.provider.DemoResolver;
import com.holybell.rpcfx.api.RpcfxResolver;
import com.holybell.rpcfx.server.RpcfxInvoker;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Configuration
@AutoConfigureBefore(HttpMessageConvertersAutoConfiguration.class)
public class Config {

    @Bean
    public RpcfxInvoker invoker() {
        // 由于标注@Configration的类会被Spring使用CGLIB提升，标注@Bean的方法不会被多次调用，无需担心 #resolver() 方法会被多次调用，
        // 导致RpcfxResolver有多个实例
        return new RpcfxInvoker(resolver());
    }

    @Bean
    public RpcfxResolver resolver() {
        return new DemoResolver();
    }

    /**
     * 覆盖Spring原生HttpMessageConverters
     * 同时兼容XML和JSON
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        SerializerFeature[] serializerFeatures = new SerializerFeature[]{
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteClassName
        };
        fastJsonConfig.setSerializerFeatures(serializerFeatures);
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        List<MediaType> mediaTypes = Collections.singletonList(MediaType.APPLICATION_JSON); // 强制指定处理JSON响应
        fastJsonHttpMessageConverter.setSupportedMediaTypes(mediaTypes);
        // 额外注册Xstream转换器
        return new HttpMessageConverters(fastJsonHttpMessageConverter, marshallingHttpMessageConverter());
    }

    @Bean
    public MarshallingHttpMessageConverter marshallingHttpMessageConverter() {
        MarshallingHttpMessageConverter marshallingHttpMessageConverter = new MarshallingHttpMessageConverter();
        marshallingHttpMessageConverter.setMarshaller(marshaller());
        marshallingHttpMessageConverter.setUnmarshaller(marshaller());
        return marshallingHttpMessageConverter;
    }

    @Bean
    public XStreamMarshaller marshaller() {
        return new XStreamMarshaller();
    }
}
