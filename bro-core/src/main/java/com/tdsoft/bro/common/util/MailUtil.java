package com.tdsoft.bro.common.util;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class MailUtil {
	public static void sendMail(String FROM, String TO, String SUBJECT, String BODY) {
		Destination destination = new Destination().withToAddresses(new String[] {TO});

		Content subject = new Content().withData(SUBJECT);
		Content textBody = new Content().withData(BODY);
		Body body = new Body().withText(textBody);
		Message message = new Message().withSubject(subject).withBody(body);

		// Assemble the email.
		SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);
		AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
		Region REGION = Region.getRegion(Regions.US_WEST_2);
		client.setRegion(REGION);
		// Send the email.
		client.sendEmail(request);
	}
}
