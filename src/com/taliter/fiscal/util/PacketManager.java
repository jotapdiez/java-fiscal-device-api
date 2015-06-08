package com.taliter.fiscal.util;

import java.io.IOException;

import com.taliter.fiscal.device.FiscalDevice;
import com.taliter.fiscal.device.FiscalPacket;

public class PacketManager{
	
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
			response = device.execute(request);
			resetResponse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean executeSuccess(){
		return response != null;
	}
	
	public int getPrinterStatus() {
		return response.getPrinterStatus();
	}

	public int getFiscalStatus() {
		return response.getFiscalStatus();
	}

	public String getString(int field) {
		return response.getString(field);
	}
	
	public void resetResponse(){
		index = 2;
	}
	
	public String nextString(){
		++index;
		return response.getString(index);
	}
	public int nextInt(){
		++index;
		return response.getInt(index);
	}
}
