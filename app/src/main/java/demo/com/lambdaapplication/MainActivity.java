package demo.com.lambdaapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


public class MainActivity extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private EditText ed1;
    private EditText ed2;
    private Button btn3;
    private Button btnSend;
    private EditText ed3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        ed1 = (EditText) findViewById(R.id.editText1);
        ed2 = (EditText) findViewById(R.id.editText2);
        btn3 = (Button) findViewById(R.id.button3);

        btn1.setOnClickListener(v -> Toast.makeText(this, "lambda", Toast.LENGTH_SHORT).show());
        btn2.setOnClickListener(v -> Toast.makeText(this, "lambda2", Toast.LENGTH_SHORT).show());
        btn3.setOnClickListener(v -> Toast.makeText(this, "lambda3", Toast.LENGTH_SHORT).show());
        initCombineLatest();

        ed3 = (EditText) findViewById(R.id.editText3);
        btnSend = (Button) findViewById(R.id.btn_send);
        initSend();
    }

    private void initSend() {
        initPhoneObservable();

        btnSend.setOnClickListener(v -> {
            //1.sendRequest 2.invoke triggerCountDown on callback method
            triggerCountDown();
        });
    }

    private void initPhoneObservable() {
        // 因为ed3只是addListener,
        // 并没有触发Observable emit数据给subscriber,
        // 所以btnSend需要将状态置成ed3还没添加监听前的状态
        btnSend.setEnabled(false);
        Observable.create((Observable.OnSubscribe<String>) subscriber ->
                ed3.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        subscriber.onNext(s.toString());
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String phoneNumber) {
                        if (phoneNumber.isEmpty()) {
                            btnSend.setEnabled(false);
                        } else {
                            //传过来的phoneNumber不为空
                            //如果已经开始获取验证码了
                            if (!btnSend.getText().equals("获取验证码")) {
                                //不处理
                            } else {
                                //正常情况只要变更了就把按钮置成可点击
                                btnSend.setEnabled(true);
                            }
                        }
                    }
                });
    }

    private void triggerCountDown() {
        int count = 10;
        Observable.interval(0, 1, TimeUnit.SECONDS)//设置0延迟，每隔一秒发送一条数据
                .take(count + 1)
                .map(countTime -> count - countTime)
                .doOnSubscribe(() -> {
                    //在发送数据的时候设置为不能点击
                    btnSend.setEnabled(false);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        btnSend.setEnabled(true);//完成了emit
                        btnSend.setText("获取验证码");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long countDownTime) {
                        btnSend.setText(countDownTime + "s后重试");
                    }
                });
    }

    private void initCombineLatest() {
        // 因为ed1,ed2只是addListener,
        // 并没有触发Observables emit数据给subscriber,
        // 所以btn3需要将状态置成ed1,ed2还没添加监听前的状态
        btn3.setEnabled(false);
        Observable<String> userObservable = Observable.create(subscriber ->
                ed1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        subscriber.onNext(s.toString());
                    }
                }));
        Observable<String> pwdObservable = Observable.create(subscriber ->
                ed2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        subscriber.onNext(s.toString());
                    }
                }));

        Observable.combineLatest(userObservable, pwdObservable, (user, pwd) -> !user.isEmpty() && !pwd.isEmpty())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean verify) {
                        Log.d("subscriber-----------", "" + verify);
                        if (verify) {
                            btn3.setEnabled(true);
                        } else {
                            btn3.setEnabled(false);
                        }
                    }
                });
    }
}
