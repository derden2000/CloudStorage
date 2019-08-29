package pro.antonshu.client.swing;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import pro.antonshu.network.message.Message;

import java.io.IOException;
import java.net.Socket;

public class NettyClient {

    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;

    public NettyClient() throws IOException {
        this.socket = new Socket("localhost", 8189);
        this.out = new ObjectEncoderOutputStream(socket.getOutputStream());
        this.in = new ObjectDecoderInputStream(socket.getInputStream(), Integer.MAX_VALUE -1);
    }

    public void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMsg(Message message) {
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Message readObject() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();
        System.out.println("ReadObject: " + obj.getClass().getSimpleName());
        return (Message) obj;
    }
}
