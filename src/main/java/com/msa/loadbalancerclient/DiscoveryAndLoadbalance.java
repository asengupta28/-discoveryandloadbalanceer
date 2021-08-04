
/*	 **************************************************************************************	****
****																						****
**** This application uses org.springframework.cloud.client.loadbalancer.LoadBalancerClient	**** 
**** to dynamically call APIs by names that are registered with Eureka Server				**** 
****																						****
**** **************************************************************************************	***/

package com.msa.loadbalancerclient;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.PathVariable;

@SpringBootApplication
@EnableDiscoveryClient

public class DiscoveryAndLoadbalance
{
	public static void main(String[] args)
	{
		SpringApplication.run(DiscoveryAndLoadbalance.class, args);
	}
}

@Controller
class AppController
{
	@Autowired
	private LoadBalancerClient loadBalancer;

	public String getEmployee(String applicationName, String url) throws RestClientException, IOException
	{
		ServiceInstance serviceInstance=loadBalancer.choose(applicationName);
		System.out.println("App1 Producer URI: " + serviceInstance.getUri());

		String baseUrl=serviceInstance.getUri().toString()+url;
		System.out.println("App1 Base URI: " + baseUrl);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;

		try
		{
			response = restTemplate.exchange(baseUrl, HttpMethod.GET, getHeaders(), String.class);
		}
		catch (Exception ex) 
		{
			System.out.println(ex);
		}

		return response.getBody();
	}

	private static HttpEntity<?> getHeaders() throws IOException
	{
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.TEXT_PLAIN_VALUE);
		return new HttpEntity<>(headers);
	}

	// This method takes two Parameters - an Application Name and application URI
	// The Application Name = Name in which the caling Application is registered in the Eureka Server
	// Look in Eureka Server's console and in the frame with tile "Instances currently registered with Eureka
	// Pick any values from column title "Application" displayed below
	// The application URI is API that will be called
 	@RequestMapping(value = "/{applicationName}/{applicationURI}", method = RequestMethod.GET, produces = {"application/json"})
	public @ResponseBody String getValueByAppName(@PathVariable("applicationName") String applicationName, @PathVariable("applicationURI") String applicationURI) throws Exception
	{
		System.out.println("Executed");
		System.out.println("From DiscoveryAndLoadbalance:findByResourceID() : Application Name = " + applicationName);
		System.out.println("From DiscoveryAndLoadbalance:findByResourceID() : Application URI = " + applicationURI);
		RestTemplate restTemplate = new RestTemplate();

		String response = getEmployee(applicationName, applicationURI);

		return (response);	//.getBody());	//"007";
	}
}