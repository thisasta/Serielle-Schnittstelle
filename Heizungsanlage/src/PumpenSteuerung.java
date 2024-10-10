package src;

import java.io.IOException;

import serialio.Serial;

public class PumpenSteuerung {

    private Serial myConnection;
    private byte[] statusPumpen;
    private byte steuerungSchalteEinAus;

    public PumpenSteuerung() {
        statusPumpen = new byte[8];
        try{
            this.myConnection = new Serial("COM1", 9600, 8, 1, 0);
            if(myConnection.open()){
                System.out.println("Systeminfo: Die Schnittstelle ist geöffnet!");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public byte[] abfragePumpenstatus() {
        try {
            while (myConnection.read() != 0x4F);
            System.out.println("Systeminfo: Startbyte wurde empfangen!\n\n");
            for (int i = 0; i < statusPumpen.length; i++) {
                statusPumpen[i] = (byte) myConnection.read();
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        System.out.println("Ausgabe der 8 empfangenen statusPumpen-Bytes:");
        System.out.print("\nBinär: \t");
        for (int i = 0; i < statusPumpen.length; i++) {
            System.out.print(String.format("%8s", Integer.toBinaryString(statusPumpen[i] & 0xFF)).replace(' ', '0') + "\t");
        }

        System.out.print("\nHexadezimal: \t");
        for (int i = 0; i < statusPumpen.length; i++) {
            System.out.print(String.format("%2s", Integer.toHexString(statusPumpen[i] & 0xFF)).replace(' ', '0') + "\t");
        }

        return statusPumpen;
    }

    public byte steuerePumpen() {
        steuerungSchalteEinAus = 0x00;
        int active = 0;

        for (int i = 0; i < statusPumpen.length; i++) {
            int time = (statusPumpen[i] >> 4) & 0x0F;
            if (time < 4) {
                if (active < 4) {
                    steuerungSchalteEinAus |= (1 << i);
                    active++;
                }
            }
        }

        for (int i = 0; i < statusPumpen.length && active < 4; i++) {
            int time = (statusPumpen[i] >> 4) & 0x0F;
            if (time == 0 && (steuerungSchalteEinAus & (1 << i)) == 0) {
                steuerungSchalteEinAus |= (1 << i);
                active++;
            }
        }

        try {
            myConnection.write(steuerungSchalteEinAus);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return steuerungSchalteEinAus;
    }



    public static void main(String[] args) {
        PumpenSteuerung pumpenSteuerung = new PumpenSteuerung();
        pumpenSteuerung.abfragePumpenstatus();
        pumpenSteuerung.steuerePumpen();
        pumpenSteuerung.abfragePumpenstatus();
    }

}
