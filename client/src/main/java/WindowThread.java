import lombok.extern.log4j.Log4j;

@Log4j
public class WindowThread extends Thread{// поток для работы основного всплывающего окна
    private Controller controller;

    public WindowThread(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            new AuthCycle(controller).encludeLoop(); //цикл регистации
            log.info("Authentication is passed");
            Network.writeObject(new ListResponce());
            // отдельный поток для отображения изменений директории клиента
            RenewClientListViewThread t = new RenewClientListViewThread(controller);
            t.setDaemon(true);
            t.start();

            new WorkCycle(controller).encludeLoop(); //цикл работы

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
