package com.example.sticky.client;

import android.util.Log;

import bistu.share.Detail;
import bistu.share.Instruction;
import bistu.share.Overview;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Client implements Serializable {

    private static Client client;

    private Socket socket;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;

    public static Client getInstance() {
        return client;
    }

    public Client(InetAddress host, int port) throws IOException {
        try {
            this.socket = new Socket(host, port);
            this.objectOut = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectIn = new ObjectInputStream(this.socket.getInputStream());
            client = this;
            Log.i(Client.class.getName(), "object stream initialized.");
        } catch (IOException e) {
            Log.e(Client.class.getName(), "IO error occurred while initializing object stream", e);
            throw e;
        }
    }

    /**
     * send SERVE_END sign to server. close object stream and socket.
     */
    public void shutdown() {
        try {
            objectOut.writeInt(Instruction.SERVE_END);
            this.objectIn.close();
            this.objectOut.close();
            this.socket.close();
            client = null;
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "IO error occurred while ending serve.", e);
        }
    }

    /**
     * get sticky overview list.
     * @return list of overview. null if any error occurred.
     */
    public List<Overview> getList() {
        List<Overview> list = null;
        try {
            objectOut.writeInt(Instruction.GET_LIST);
            objectOut.flush();

            if (objectIn.readInt() == Instruction.FINE_CODE) {
                Object o = objectIn.readObject();
                list = (List) o;
            } else {
                Log.e(this.getClass().getName(), "Server error while getting List.");
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(this.getClass().getName(), "IO error occurred while getting list.", e);
        }
        return list;
    }

    /**
     * get the detail of sticky specified by id.
     * @param id the sticky's id
     * @return detail of sticky, null if any error occurred.
     */
    public Detail getDetail(long id) {
        Detail d = null;
        try {
            objectOut.writeInt(Instruction.GET_DETAIL);
            objectOut.writeLong(id);
            objectOut.flush();

            if (objectIn.readInt() == Instruction.FINE_CODE)
                d = (Detail)objectIn.readObject();
            else
                Log.e(this.getClass().getName(), "Server error while getting Detail.");
        } catch (IOException | ClassNotFoundException e) {
            Log.e(this.getClass().getName(), "IO error occurred while getting detail.", e);
        }
        return d;
    }

    /**
     * add a new sticky.
     * @return the id of new sticky. null if any error occurred.
     */
    public long addSticky() {
        long id = -1;
        try {
            objectOut.writeInt(Instruction.ADD_STICKY);
            objectOut.flush();

            if(objectIn.readInt() == Instruction.FINE_CODE)
                id = objectIn.readLong();
            else
                Log.e(this.getClass().getName(), "Server error while adding sticky.");
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "IO error occurred while adding sticky.", e);
        }
        return id;
    }

    /**
     * update a sticky.
     * @param d the sticky with new content.
     * @return true if succeed, false if any error occurred.
     */
    public boolean updateSticky(Detail d) {
        boolean result = false;
        try {
            Log.i(this.getClass().getName(), String.format("updating, %s", d.toString()));
            objectOut.writeInt(Instruction.UPDATE_STICKY);
            objectOut.writeUnshared(d);
            objectOut.flush();

            if (objectIn.readInt() == Instruction.FINE_CODE) {
                result = true;
                Log.i(this.getClass().getName(), String.format("success update, %s", d.toString()));
            } else
                Log.e(this.getClass().getName(), "Server error while updating sticky.");
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "IO error occurred while updating sticky", e);
        }
        return result;
    }

    /**
     * remove a sticky specified by id
     * @param id the id of sticky
     * @return true if succeed, false if any error occurred.
     */
    public boolean removeStick(long id) {
        boolean result = false;
        try {
            objectOut.writeInt(Instruction.REMOVE_STICKY);
            objectOut.writeLong(id);
            objectOut.flush();

            if (objectIn.readInt() == Instruction.FINE_CODE)
                result = true;
            else
                Log.e(this.getClass().getName(), "Server error while removing sticky.");
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "IO error occurred while removing sticky.", e);
        }
        return result;
    }

}
