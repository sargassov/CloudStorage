import java.io.IOException;

public class RenewClientListViewThread extends Thread{ //отдельный тред для обновления списка файлов на клиенте. Раз в 3 секунды

    private Controller controller;

    public RenewClientListViewThread(Controller controller){
        this.controller = controller;
    }

    @Override
    public void run() {
        while (true){
                try {
                    controller.refreshClientView();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

}
