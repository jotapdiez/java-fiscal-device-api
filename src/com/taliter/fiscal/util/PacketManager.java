package com.taliter.fiscal.util;

import java.io.IOException;

import com.taliter.fiscal.device.FiscalDevice;
import com.taliter.fiscal.device.FiscalPacket;
import com.taliter.fiscal.device.hasar.HasarConstants;

public class PacketManager implements HasarConstants {
	
	private static FiscalDevice device = null;
	public static void setDevice(FiscalDevice device) {
		PacketManager.device = device;
	}
	
	public static PacketManager create(int command){
		if (device == null){
			System.err.println("No hay dispositivo fiscal asociado.");
			return new PacketManager();
		}
		return new PacketManager(command);
	}
	
	FiscalPacket request = null;
	FiscalPacket response = null;
	
	int index = 0;
	private PacketManager() {
		
	}
	
	private PacketManager(int command) {
		request = device.createFiscalPacket();
		request.setCommandCode(command);
	}
	
	public PacketManager addString(String value){
		request.setString(++index, value);
		return this;
	}
	public PacketManager addInt(int value){
		request.setInt(++index, value);
		return this;
	}
	public PacketManager addDouble(double value){
		request.setDouble(++index, value);
		return this;
	}

	public void execute() {
		try {
			System.out.println("**************************************");
			System.out.println("Request: " + request);
			System.out.flush();
			response = device.execute(request);
			System.out.println("response: " + response);
			resetResponse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean executeSuccess(){
		return response != null;
	}
	
	private int[] printerStatusCodes = { PST_PRINTER_BUSY, PST_PRINTER_ERROR, PST_PRINTER_OFFLINE,
			 PST_JOURNAL_PAPER_OUT, PST_TICKET_PAPER_OUT, PST_PRINT_BUFFER_FULL, 
			 PST_PRINT_BUFFER_EMPTY, PST_PRINTER_COVER_OPEN, PST_MONEY_DRAWER_CLOSED
			};
	/** Códigos de mensajes de estado del controlador fiscal */
	private int[] fiscalStatusCodes  = { FST_FISCAL_MEMORY_CRC_ERROR, FST_WORKING_MEMORY_CRC_ERROR, FST_UNKNOWN_COMMAND,    
				 FST_INVALID_DATA_FIELD, FST_INVALID_COMMAND, FST_ACCUMULATOR_OVERFLOW,
				 FST_FISCAL_MEMORY_FULL, FST_FISCAL_MEMORY_ALMOST_FULL, FST_DEVICE_CERTIFIED,
				 FST_DEVICE_FISCALIZED, FST_CLOCK_ERROR, FST_FISCAL_DOCUMENT_OPEN,
				 FST_DOCUMENT_OPEN, FST_STATPRN_ACTIVE
			};
	String[] bitsPrinterStatus = new String[]{"","","Error de impresora","Impresora offline","Falta papel del diario","Falta papel de tiques",
			 "Buffer de impresora lleno","Buffer de impresora vacío","Tapa de impresora abierta","","",
			 "","","","Cajón de dinero cerrado o ausente", "TODO"};
	
	String[] bitsFiscalStatus = new String[]{"Error en chequeo de memoria fiscal","Error en chequeo de memoria de trabajo","Carga de batería baja","Comando desconocido","Datos no válidos en un campo","Comando no válido para el estado fiscal actual",
			 "Desborde del Total","Memoria fiscal llena, bloqueada o dada de baja","Memoria fiscal a punto de llenarse","Terminal fiscal certificada","Terminal fiscal fiscalizada",
			 "Error en ingreso de fecha","Documento fiscal abierto","Documento abierto","", "TODO"};

	public int getPrinterStatus() {
		int status = response.getPrinterStatus();
		
		parseBits(printerStatusCodes, bitsPrinterStatus, status, "IMPRESORA");
		
		return status;
	}

	public int getFiscalStatus() {
		int status = response.getFiscalStatus();
	
		parseBits(fiscalStatusCodes, bitsFiscalStatus, status, "CONTROLADOR FISCAL");
		
		return status;
	}

	private void parseBits(int[] statusCodes, String[] bits, int status, String onErrorMsg){
		if ((status & FST_BITWISE_OR)==0){
			System.out.println("Status OK");
			return;
		}
		
//		System.out.println("Status:" + status);
		for (int i=0 ; i<statusCodes.length ; ++i){
			String bit = bits[i];
			if (bit.isEmpty()){ //vacios = no implementados por hasar (Dice "Siempre 0" en el PDF)
				continue;
			}
			
			int code = statusCodes[i];
			int res = (status & code);
//			System.out.println("(status & "+statusCodes[i]+")="+res);
			if (res != 0){
				System.out.println("*** ERROR "+ onErrorMsg +" *** "+statusCodes[i]+": " + bits[i]);
//				System.err.flush();
			}
		}
	}
	
	public String getString(int field) {
		return response.getString(field);
	}
	
	public void resetResponse(){
		index = 2; //0=Comando enviado -- 1=Printer Status -- 2=FiscalStatus
	}
	
	public static String RESPONSE_INVALID_PARAM_STRING = "INVALIDO"; 
	public static int RESPONSE_INVALID_PARAM_INT = -100;
	
	public String nextString(){
//		System.out.println("PM ****** Current index: " + index);
//		System.out.println("PM ****** response.getSize(): " + response.getSize());
		index++;
		if (response.getSize()<=index){
			System.out.println("*** WARNING *** Pedido proxima string (Item: "+index+" -- Size: "+response.getSize()+") pero no existia");
//			System.err.flush();
			return RESPONSE_INVALID_PARAM_STRING;
		}
		
		return response.getString(index);
	}
	public int nextInt(){
//		System.out.println("PM ****** Current index: " + index);
//		System.out.println("PM ****** response.getSize(): " + response.getSize());
		index++;
		if (response.getSize()<=index){
			System.out.println("*** WARNING *** Pedido proximo int (Item: "+index+" -- Size: "+response.getSize()+") pero no existia");
//			System.err.flush();
			return RESPONSE_INVALID_PARAM_INT;
		}
		
		return response.getInt(index);
	}
}
