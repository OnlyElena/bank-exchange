///**
// * @author: OnlyElena@mail.ru
// * Date: 3/18/16
// * Time: 7:37 PM
// */
////import android.os.Bundle;
////import android.app.Activity;
////import android.util.Log;
////import android.view.View;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.TextView;
//
//public class MainActivity extends Activity {
//
//    Button butGetResult;
//    EditText editResult;
//    TextView textViewInfo;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        butGetResult = (Button) findViewById(R.id.buttonGetResult);
//        editResult = (EditText) findViewById(R.id.editText);
//        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
//
//        new NewThread(); // создаём новый поток
//
//        try {
//            for (int i = 5; i > 0; i--) {
//                System.out.println("Главный поток: " + i);
//                Thread.sleep(1000);
//            }
//        } catch (InterruptedException e) {
//            System.out.println("Главный поток прерван");
//        }
//        System.out.println("Главный поток завершён");
//
//    }
//
//    public void onClick(View v) {
//    }
//
//    // Создание второго потока
//    class NewThread implements Runnable {
//        Thread thread;
//
//        // Конструктор
//        NewThread() {
//            // Создаём новый второй поток
//            thread = new Thread(this, "Поток для примера");
//            System.out.println("Создан второй поток " + thread);
//            thread.start(); // Запускаем поток
//        }
//
//        // Обязательный метод для интерфейса Runnable
//        public void run() {
//            try {
//                for (int i = 5; i > 0; i--) {
//                    System.out.println("Второй поток: " + i);
//                    Thread.sleep(500);
//                }
//            } catch (InterruptedException e) {
//                System.out.println("Второй поток прерван");
//            }
//            System.out.println("Второй поток завершён");
//        }
//    }
//}