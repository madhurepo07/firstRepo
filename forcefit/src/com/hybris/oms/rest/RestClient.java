/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.hybris.oms.rest;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.csvreader.CsvReader;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestClient {
	static long lStartTime;
	//https://omstmppprd.tataunistore.com/oms-ext-web/webresources/order/cancellable
	//https://oms.tatacliq.com/oms-ext-web/webresources/order/cancellable
	public static void main(final String[] args) throws Exception {

		System.out.println("Enter filePath");
		final Scanner sc = new Scanner(System.in);
		final String path = sc.nextLine();
		FileReader fileReader = new FileReader(path);
		CsvReader records = new CsvReader(fileReader);
		if (records.readHeaders()) {

			for (final String headerField : records.getHeaders()) {
				System.out.print(headerField);
			}
		}
		final ExecutorService executor = Executors.newFixedThreadPool(100);
		while (records.readRecord()) {

			executor.execute(updateOrderLine(records.get("orderid"), records.get("orderlineid"),
					records.get("primarylogisticsid"), records.get("primaryslaveid")));
			Thread.sleep(2000);
		}

		System.out.println("Done !!!!!!!!! ");
		records.close();
		fileReader.close();
		sc.close();

	}

	public static Thread updateOrderLine(String orderid, String orderlineid,
			String primarylogisticsid, String primaryslaveid) {

		return new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {
					final Client client = Client.create();

					final WebResource webResource = client.resource(url);
					Date today = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					//SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
					String timeStamp = formatter.format(today);
					String orderStatusUpdateQuery="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><orderLineBUC><orderId>" + orderid + "</orderId><transactionId>" + orderlineid+ "</transactionId><primarySlaveID>"+primaryslaveid+"</primarySlaveID><primaryLogisticID>"+ primarylogisticsid +"</primaryLogisticID></orderLineBUC>";
										
					System.out.println(orderStatusUpdateQuery);
					final ClientResponse response = webResource.accept("application/xml").type("application/xml")
							.header("X-tenantId", "single").post(ClientResponse.class, orderStatusUpdateQuery);

					System.out.println("status***** : " + response.getStatus()+" for orderlineid :"+orderlineid+" for primarySlaveID :"+primaryslaveid+"for primaryLogisticID"+primarylogisticsid);

				} catch (final Exception e) {
					System.out.println(e.getMessage() + " " + orderlineid);
				}
			}
		});

	}

}
