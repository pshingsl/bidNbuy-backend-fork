//package com.bidnbuy.server.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//
//import java.io.IOException;
//
//@Configuration
//public class FcmConfig {
//
//    @Value("${fcm.service.account.file}")
//    private String serviceAccountPath;
//
//    //시작시 firebase admin sdk초기화
//    @PostConstruct
//    public void initialize() {
//        try{
//            //json파일 읽어오기
//            ClassPathResource resource = new ClassPathResource(serviceAccountPath);
//            //옵션 빌더 설정
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
//                    .build();
//            //firebaseapp초기화 중복 방지
//            if(FirebaseApp.getApps().isEmpty()){
//                FirebaseApp.initializeApp(options);
//                //성공적 초기화
//            }
//        }catch (IOException e){
//            throw new RuntimeException("fcm 초기화 오류-초기화 실패", e);
//        }catch (Exception e){
//            throw new RuntimeException("fcm 초기화 오류", e);
//        }
//    }
//}
