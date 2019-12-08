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
    private static InetAddress address;
    private static int port;

    private Socket socket;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;

    public static Client getInstance() {
        return client;
    }
    public static boolean init(InetAddress host, int p) {
        try {
            client = new Client(host, p);
            address = host;
            port = p;
        } catch (IOException e) {
            Log.e(Client.class.getName(), "fail to init client.");
            return false;
        }
        return true;
    }

    private Client(InetAddress host, int port) throws IOException {
        try {
            this.socket = new Socket(host, port);
            this.objectOut = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectIn = new ObjectInputStream(this.socket.getInputStream());
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
        return this.getList(true);
    }

    private List<Overview> getList(boolean retry) {
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
            if (retry && reconnect())
                return this.getList(false);
        }
        return list;
    }

    /**
     * get the detail of sticky specified by id.
     * @param id the sticky's id
     * @return detail of sticky, null if any error occurred.
     */
    public Detail getDetail(long id) {
        return this.getDetail(true, id);
    }

    private Detail getDetail(boolean retry, long id) {
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
            if (retry && reconnect())
                return this.getDetail(false, id);
        }
        return d;
    }

    /**
     * add a new sticky.
     * @return the id of new sticky. -1 if any error occurred.
     */
    public long addSticky() {
        return this.addSticky(true);
    }

    private long addSticky(boolean retry) {
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
            if (retry && reconnect())
                return this.addSticky(false);
        }
        return id;
    }

    /**
     * update a sticky.
     * @param d the sticky with new content.
     * @return true if succeed, false if any error occurred.
     */
    public boolean updateSticky(Detail d) {
        return this.updateSticky(true, d);
    }

    private boolean updateSticky(boolean retry, Detail d) {
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
            if (retry && reconnect())
                return this.updateSticky(false, d);
        }
        return result;
    }

    /**
     * remove a sticky specified by id
     * @param id the id of sticky
     * @return true if succeed, false if any error occurred.
     */
    public boolean removeStick(long id) {
        return this.removeStick(true, id);
    }

    private boolean removeStick(boolean retry, long id) {
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
            if (retry && reconnect())
                return this.removeStick(false, id);
        }
        return result;
    }

    private boolean reconnect() {
        int i = 0;
        while (!init(address, port) && i++ < 3) {
            Log.e(this.getClass().getName(), String.format("reconnect fail. retry %d time.", 3));
        }
        if (i >= 3) {
            Log.e(this.getClass().getName(), "reconnect failed after 3 times try.");
            return false;
        } else {
            Log.i(this.getClass().getName(), "reconnected, retry doing the operate.");
            return true;
        }
    }

}
