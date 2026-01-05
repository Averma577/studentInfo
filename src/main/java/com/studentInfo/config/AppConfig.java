package com.studentInfo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@ComponentScan(basePackages = "com.studentInfo")
@PropertySource("classpath:database.properties")
public class AppConfig implements WebMvcConfigurer {

    private final Environment env;

    public AppConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {

        // Check all possible upload locations
        String[] possiblePaths = {
                System.getProperty("user.dir"),
                "C:/Users/hp/.SmartTomcat/StudentInfo",
                "C:/Users/hp/.SmartTomcat/StudentInfo/StudentInfo",
                System.getProperty("catalina.base"),
                System.getProperty("catalina.home")
        };

        for (String path : possiblePaths) {
            if (path != null) {
                File baseDir = new File(path);

                if (baseDir.exists()) {
                    // Check for uploads directory
                    File uploadsDir = new File(baseDir, "uploads");

                    if (uploadsDir.exists()) {
                        File[] files = uploadsDir.listFiles();
                        System.out.println("  Files in uploads: " + (files != null ? files.length : 0));
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                System.out.println("    - " + file.getName() + " (" + file.length() + " bytes)");
                            }
                        }
                    }
                }
            }
            System.out.println("---");
        }
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.username"));
        dataSource.setPassword(env.getProperty("jdbc.password"));

        // Connection pool configuration
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(20);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(5);
        dataSource.setMaxWaitMillis(10000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(5242880); // 5MB
        resolver.setMaxUploadSizePerFile(5242880); // 5MB per file
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Set this to EXACTLY match where Spring is looking (from your logs)
        String uploadPath = "C:/Users/hp/.SmartTomcat/StudentInfo/StudentInfo/uploads/";


        // Create the directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
        }


        // Configure the resource handler
        // IMPORTANT: Use forward slashes for Spring, even on Windows
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath.replace("\\", "/"))
                .setCachePeriod(3600);


        // Also configure static resources
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/", "classpath:/static/")
                .setCachePeriod(3600);

        // For webjars (if you use Bootstrap/jQuery via webjars)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/**")
                .addResourceLocations("/");
    }

    @Bean
    public String createUploadDirectories() {

        // Create uploads in multiple locations to be safe
        String[] locations = {
                "C:/Users/hp/.SmartTomcat/StudentInfo/StudentInfo/uploads",
                System.getProperty("user.dir") + "/uploads",
                System.getProperty("user.dir") + "/StudentInfo/uploads"
        };

        for (String location : locations) {
            File dir = new File(location);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
            } else {

                // List files
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        System.out.println("    - " + file.getName());
                    }
                }
            }
        }

        return "Upload directories created";
    }
}