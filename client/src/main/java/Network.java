import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network { //свод статических методов, отвечающих за взаимодействие с сервером
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private static int MAX_FILE_SIZE = 209715200;

    public static ObjectDecoderInputStream getIn() {
        return in;
    }

    public static ObjectEncoderOutputStream getOut() {
        return out;
    }

    static void start() { //инициация входящего и исходящего каналов
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), MAX_FILE_SIZE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket() {
        return socket;
    }

    static void stop() { //закрытие сокета и каналов
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

    static void writeObject(Command msg) { //запись объекта на сервер
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Command readObject() throws ClassNotFoundException, IOException {//чтение с сервера
        Object obj = in.readObject();
        return (Command) obj;
    }
}
