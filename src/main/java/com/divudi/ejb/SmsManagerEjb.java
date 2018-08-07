/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.entity.Sms;
import com.divudi.facade.EmailFacade;
import com.divudi.facade.SmsFacade;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.swing.JOptionPane;

/**
 *
 * @author Buddhika
 */
@Stateless
public class SmsManagerEjb {

    @EJB
    private EmailFacade emailFacade;
    @EJB
    SmsFacade smsFacade;

    @SuppressWarnings("unused")
    @Schedule(second = "19", minute = "*/5", hour = "*", persistent = false)
    public void myTimer() {
        sendSmsAwaitingToSendInDatabase();
    }

    private void sendSmsAwaitingToSendInDatabase() {
        String j = "Select e from Sms e where e.sentSuccessfully=false and e.retired=false";
        List<Sms> smses = getSmsFacade().findBySQL(j);
//        if (false) {
//            Sms e = new Sms();
//            e.getSentSuccessfully();
//            e.getInstitution();
//        }
        for (Sms e : smses) {
            e.setSentSuccessfully(Boolean.TRUE);
            getSmsFacade().edit(e);

            sendSms(e.getReceipientNumber(), e.getSendingMessage(),
                    e.getInstitution().getSmsSendingUsername(),
                    e.getInstitution().getSmsSendingPassword(),
                    e.getInstitution().getSmsSendingAlias());
            e.setSentSuccessfully(true);
            e.setSentAt(new Date());
            getSmsFacade().edit(e);
        }

    }

    public String executePost(String targetURL, Map<String, String> parameters) {
        System.out.println("executePost");
        HttpURLConnection connection = null;
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "?";
        }
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String pVal;
            try {
                pVal = java.net.URLEncoder.encode(m.getValue().toString(),"UTF-8");
            } catch (UnsupportedEncodingException ex) {
                pVal="";
                Logger.getLogger(SmsManagerEjb.class.getName()).log(Level.SEVERE, null, ex);
            }
            String pPara = (String) m.getKey();
            targetURL += pPara + "=" + pVal.toString() + "&";
        }
        
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "last=true";
        }

        try {
            System.out.println("targetURL = " + targetURL);
            //Create connection
            System.out.println("1");
            URL url = new URL(targetURL);
            System.out.println("2");
            connection = (HttpURLConnection) url.openConnection();
            System.out.println("3");
            connection.setRequestMethod("GET");
            System.out.println("4");
            connection.setDoOutput(true);
            System.out.println("4");
            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            System.out.println("5");
            wr.writeBytes(targetURL);
            System.out.println("6");
            wr.flush();
            System.out.println("wr = " + wr);
            System.out.println("7");
            wr.close();
            System.out.println("8");
            //Get Response  
            InputStream is = connection.getInputStream();
            System.out.println("is = " + is);
            System.out.println("9");
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            System.out.println("rd = " + rd);
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            System.out.println("e = " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void sendSms(String number, String message, String username, String password, String sendingAlias) {

        System.out.println("number = " + number);
        System.out.println("message = " + message);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        System.out.println("sendingAlias = " + sendingAlias);

        Map<String,String> m= new HashMap();
        m.put("userName", username);
        m.put("password", password);
        m.put("userAlias", sendingAlias);
        m.put("number", number);
        m.put("message", message);

        String res = executePost("http://localhost:21599/sms/faces/index.xhtml", m);
//        res = executePost("http://localhost:8080/sms/faces/index.xhtml", m);
        System.out.println("res = " + res);
        if (res == null) {
            System.out.println("Error in sending sms as res is null");
        } else if (res.toUpperCase().contains("200")) {
            System.out.println("sms sent");
        } else {
            System.out.println("Error in sending sms as do not contain 200");
        }

    }

    public SmsFacade getSmsFacade() {
        return smsFacade;
    }

}