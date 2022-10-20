package com.translink.api.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.translink.api.config.format.SpecializedTimeFormatter;
import com.translink.api.config.format.SpecializedTimeSerializer;
import com.translink.api.config.format.converter.SpecializedTimeReadConverter;
import com.translink.api.config.format.converter.SpecializedTimeWriteConverter;
import com.translink.api.config.format.model.SpecializedTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
public class AppConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new SpecializedTimeFormatter());
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new SpecializedTimeWriteConverter());
        converters.add(new SpecializedTimeReadConverter());

        return new MongoCustomConversions(converters);
    }

    @Bean
    public SimpleModule specializedTimeModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(SpecializedTime.class, new SpecializedTimeSerializer());

        return simpleModule;
    }
}
