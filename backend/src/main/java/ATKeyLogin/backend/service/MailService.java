package ATKeyLogin.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import ATKeyLogin.backend.model.RescueEmailTemplate;
import ATKeyLogin.backend.model.RetailLicenseTemplate;
import ATKeyLogin.backend.model.SignUpEmailTemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String NOREPLY_ADDRESS = "customer.support@authentrend.com";

    @Value("${server.url}")
    private String serverUrl;

    @Value("${SendGrid.apiKey}")
    private String sendGridApiKey;

    public String sendSignUpEmail(String reciever, Integer veriCode, String language) throws IOException{

            Mail mail = new Mail();
            SignUpEmailTemplate signUpEmailTemplate = new SignUpEmailTemplate(language);
		    Email from = new Email();
		    String subject = signUpEmailTemplate.getSubject();
		    Email to = new Email();
            Email cc1 = new Email();
            Email cc2 = new Email();
		    Content content = new Content();
            
		    SendGrid sg = new SendGrid(sendGridApiKey);
		    Request request = new Request();

            Personalization personalization = new Personalization();

            from.setName("customer.support@authentrend.com");
            from.setEmail("customer.support@authentrend.com");
            to.setName(reciever);
            to.setEmail(reciever);

            // cc1.setName("Sean");
            // cc1.setEmail("xxx@authentrend.com");
            // cc1.setName("Elia");
            // cc1.setEmail("elia.ku@authentrend.com");

            // personalization.addCc(cc1);
            // personalization.addCc(cc2);
            personalization.addTo(to);
            mail.setFrom(from);
            mail.setSubject(subject);

            String encodedE = Base64.getUrlEncoder().encodeToString(reciever.getBytes());

            String encodedV = Base64.getUrlEncoder().encodeToString(veriCode.toString().getBytes());

            String t = String.format("signupEmail=%s&code=%s", encodedE, encodedV);

            String redirectURL = String.format("%s/login?%s", serverUrl, t);

            String text = String.format(signUpEmailTemplate.getBody(), reciever, veriCode, redirectURL);

            content.setType("text/html");
            content.setValue(text);
            mail.addContent(content);
            
            mail.addPersonalization(personalization);
            try {
                
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);

                if (response.getStatusCode() != 202 && response.getStatusCode() != 200) {
                    log.error("body = {}", response.getBody());
                    log.error("status = {}", response.getStatusCode());  

                    throw new IOException(response.getBody());
                }

                return reciever;
            } catch (IOException ex) {
                throw ex;
            } 
            
	}

    public String sendRetailLCEmail(String reciever, String licenseCode, String lang) throws IOException{

            Mail mail = new Mail();
            RetailLicenseTemplate retailLicenseTemplate = new RetailLicenseTemplate(lang);
		    Email from = new Email();
		    String subject = retailLicenseTemplate.getSubject();
		    Email to = new Email();
            Email cc1 = new Email();
            Email cc2 = new Email();
		    Content content = new Content();
            
		    SendGrid sg = new SendGrid(sendGridApiKey);
		    Request request = new Request();

            Personalization personalization = new Personalization();

            from.setName("customer.support@authentrend.com");
            from.setEmail("customer.support@authentrend.com");
            to.setName(reciever);
            to.setEmail(reciever);

            personalization.addTo(to);
            mail.setFrom(from);
            mail.setSubject(subject);   
            String text = String.format(retailLicenseTemplate.getBody(), reciever, licenseCode , serverUrl);
            content.setType("text/html");
            content.setValue(text);
            mail.addContent(content);
            
            mail.addPersonalization(personalization);
            try {
                
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                if (response.getStatusCode() != 202 && response.getStatusCode() != 200) {
                    log.error("body = {}", response.getBody());
                    log.error("status = {}", response.getStatusCode());  

                    throw new IOException(response.getBody());
                }

                return reciever;
            } catch (IOException ex) {
                throw ex;
            } 
            
	}


    public String sendRescueEmail(String reciever, Integer veriCode, String language) throws IOException{

            Mail mail = new Mail();
		    Email from = new Email();
            RescueEmailTemplate rescueEmailTemplate = new RescueEmailTemplate(language);
		    String subject = rescueEmailTemplate.getSubject();
		    Email to = new Email();
            Email cc1 = new Email();
            Email cc2 = new Email();
		    Content content = new Content();
            
		    SendGrid sg = new SendGrid(sendGridApiKey);
		    Request request = new Request();

            Personalization personalization = new Personalization();

            from.setName("customer.support@authentrend.com");
            from.setEmail("customer.support@authentrend.com");
            to.setName(reciever);
            to.setEmail(reciever);

            // cc1.setName("Sean");
            // cc1.setEmail("xxx@authentrend.com");
            // cc1.setName("Elia");
            // cc1.setEmail("elia.ku@authentrend.com");
            // cc2.setName("Carrine");
            // cc2.setEmail("carrine.shih@authentrend.com");

            // personalization.addCc(cc1);
            // personalization.addCc(cc2);
            personalization.addTo(to);
            mail.setFrom(from);
            mail.setSubject(subject);

            
            String encodedE = Base64.getUrlEncoder().encodeToString(reciever.getBytes());

            String encodedV = Base64.getUrlEncoder().encodeToString(veriCode.toString().getBytes());

            String t = String.format("rescueEmail=%s&code=%s", encodedE, encodedV);

            String redirectURL = String.format("%s/rescue?%s", serverUrl, t);

            String text = String.format(rescueEmailTemplate.getBody(), veriCode, redirectURL);

            content.setType("text/html");
            content.setValue(text);
            mail.addContent(content);
            
            mail.addPersonalization(personalization);
            try {
                
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);

                if (response.getStatusCode() != 202 && response.getStatusCode() != 200) {
                    log.error("body = {}", response.getBody());
                    log.error("status = {}", response.getStatusCode());  

                    throw new IOException(response.getBody());
                }

                return reciever;
            } catch (IOException ex) {
                throw ex;
            } 
            
	}
}


