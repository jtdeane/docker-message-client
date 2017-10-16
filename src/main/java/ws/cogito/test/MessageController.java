package ws.cogito.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

import javax.jms.TextMessage;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Endpoint submitting test messages
 * @author jeremydeane
 */
@RestController
@RequestMapping("/")
public class MessageController {
	
	private static final Logger logger = LoggerFactory.getLogger
			(MessageController.class);
	
	@Autowired
	private ApplicationContext context;
	
	/**
	 * Process Event
	 * @throws Exception
	 */
	@RequestMapping(value = "message/{message}", method=RequestMethod.GET)
	public String handleEvent (@PathVariable String message, 
			HttpServletResponse response) throws Exception {
		
		logger.info("Received " + message);
		
		//argument determines the queue or topic
		if (message != null && message.length() > 0) {
			
			switch (message) {
			
			case "magic.alerts":
				
				publishToTopic(context);
				
				break;
				
			case "magic.order":
				
				sendToQueue("order.xml", "magic.order", context);
				
				break;
				
			case "magic.orders":
				
				sendToQueue("orders.xml", "magic.orders", context);
				
				break;
				
			case "emagic.orders":
				
				sendToQueue("orders.xml", "emagic.orders", context);
				
				break;
				
			case "emagic.bad":
				
				sendToQueue("bad-batch.xml", "emagic.orders", context);
				
				break;
				
			case "unique.orders":
				
				idempotentMessages(context);;
				
				break;

			default:
				
				sendAlchemyAlert(context);
				
				break;
			}			
			
		} else {
			
			//no argument passed
			sendAlchemyAlert(context);
		}
		
		response.setStatus(HttpStatus.ACCEPTED.value());
		
		response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");
		
		return "Processing: " + message;
	}
	
	/**
	 * Send a Test Alert to a Queue
	 * @param context
	 */
	private static void sendAlchemyAlert (ApplicationContext context) {
		
		logger.debug("Sending Message to Queue: test.alchemy");

		//Destination set in message-client-spring.xml - default
		JmsTemplate jmsTemplate = (JmsTemplate) context.getBean("jmsQueueTemplate");
		
		/*
		 * http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jms/core/MessageCreator.html
		 */
		jmsTemplate.send(session -> {
			
            TextMessage message = session.createTextMessage
            		("Alchemy is forbidden; Magic is encouraged"); 

            message.setStringProperty("Mime Type", "text/html");
            
            return message;
			
		});
		
		logger.debug("Check: http://localhost:8161/admin/queues.jsp");
	}
	
	/**
	 * Send message to a Queue
	 * @param file
	 * @param destination
	 * @param context
	 */
	private static void sendToQueue(String file, String destination, ApplicationContext context) {
		
		logger.debug("Sending " + file + " to Queue: " + destination);
		
		JmsTemplate jmsTemplate = (JmsTemplate) context.getBean("jmsQueueTemplate");
		
		jmsTemplate.send(destination, session -> {
			
            TextMessage message = session.createTextMessage (getXmlFromFile(file)); 

            message.setStringProperty("Mime Type", "application/xml");
            
            return message;
			
		});
		
		logger.debug("Check: http://localhost:8161/admin/queues.jsp");
	}
		
	/**
	 * Publish message to Topic
	 * @param context
	 */
	private static void publishToTopic(ApplicationContext context) {
		
		logger.debug("Publish Alert to Topic: magic.alerts");
		
		//destination set in message-client-spring.xml - default
		JmsTemplate jmsTemplate = (JmsTemplate) context.getBean("jmsTopicTemplate");
		
		jmsTemplate.send(session -> {
			
            TextMessage message = session.createTextMessage
					("Unauthorized use of magic supplies - Dice");
		
			message.setStringProperty("Mime Type", "text/html");
			message.setStringProperty("Snatcher", "Fenrir Greyback");
            
            return message;
			
		});		
		
		logger.debug("Check: http://localhost:8161/admin/topics.jsp");
	}
	
	/**
	 * Send three messages with the third a duplicate. One first and third
	 * should make it to magic.order
	 * @param context
	 */
	private static void idempotentMessages (ApplicationContext context) {
		
		String uuid = UUID.randomUUID().toString();
		String order = getXmlFromFile("order.xml");
		
		logger.debug("FIRST Message with Unique ID " + uuid 
				+" to Queue: " + "unique.order");
		
		JmsTemplate jmsTemplate = (JmsTemplate) context.getBean("jmsQueueTemplate");
		
		jmsTemplate.send("unique.order", session -> {
			
            TextMessage message = session.createTextMessage (order);
            
            message.setStringProperty("uniqueId", uuid);
            message.setStringProperty("Mime Type", "application/xml");
            
            return message;
			
		});
		
		logger.debug("SECOND Message with SAME Unique ID " + uuid 
				+" to Queue: " + "unique.order");
		
		jmsTemplate.send("unique.order", session -> {
			
            TextMessage message = session.createTextMessage (order); 

            message.setStringProperty("uniqueId", uuid);
            message.setStringProperty("Mime Type", "application/xml");
            
            return message;
			
		});
		
		String uuid2 = UUID.randomUUID().toString();
		
		logger.debug("THIRD Message with Unique ID " + uuid2 
				+" to Queue: " + "unique.order");
		
		jmsTemplate.send("unique.order", session -> {
			
            TextMessage message = session.createTextMessage (order); 

            message.setStringProperty("uniqueId", uuid2);
            message.setStringProperty("Mime Type", "application/xml");
            
            return message;
			
		});
		
	}	
	
    /**
     * Helper method for retrieving message content from file
     * @param fileName
     * @return String
     */
    private static String getXmlFromFile(String fileName) {
        String xml = null;
        StringBuffer text = new StringBuffer();
        String line = null;
        
        try (BufferedReader in = new BufferedReader
        		(new InputStreamReader(MessageController.class
                .getResourceAsStream(fileName)))) {
        	
            while ((line = in.readLine()) != null) {
                text.append(line);
            }

            xml = text.toString();
			
		} catch (Exception e) {
			
			logger.error("Failed to read test file " + fileName + " " + e.toString());
		}

        return xml;
    }	
}