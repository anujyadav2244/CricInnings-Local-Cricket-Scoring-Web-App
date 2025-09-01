package com.cricbook.cricbook.cloudinary;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        // Reads CLOUDINARY_URL from environment variables
        return new Cloudinary(ObjectUtils.emptyMap());
    }
}
