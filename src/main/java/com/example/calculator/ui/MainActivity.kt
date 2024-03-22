package com.example.calculator.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val publishSubjectOperands = PublishSubject.create<String>()
    private val publishSubjectOperator = PublishSubject.create<String>()
    private var disposableOperands : Disposable? = null
    private var disposableOperator : Disposable? = null
    private var flagOP : Boolean = false
    private var sequence = StringBuilder("0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bt0.setOnClickListener {
            publishSubjectOperands.onNext("0")
        }
        binding.bt1.setOnClickListener {
            publishSubjectOperands.onNext("1")
        }
        binding.bt2.setOnClickListener {
            publishSubjectOperands.onNext("2")
        }
        binding.bt3.setOnClickListener {
            publishSubjectOperands.onNext("3")
        }
        binding.bt4.setOnClickListener {
            publishSubjectOperands.onNext("4")
        }
        binding.bt5.setOnClickListener {
            publishSubjectOperands.onNext("5")
        }
        binding.bt6.setOnClickListener {
            publishSubjectOperands.onNext("6")
        }
        binding.bt7.setOnClickListener {
            publishSubjectOperands.onNext("7")
        }
        binding.bt8.setOnClickListener {
            publishSubjectOperands.onNext("8")
        }
        binding.bt9.setOnClickListener {
            publishSubjectOperands.onNext("9")
        }

        binding.add.setOnClickListener {
            publishSubjectOperator.onNext("+")
        }
        binding.subtract.setOnClickListener {
            publishSubjectOperator.onNext("-")
        }
        binding.multiply.setOnClickListener {
            publishSubjectOperator.onNext("*")
        }
        binding.divide.setOnClickListener {
            publishSubjectOperator.onNext("/")
        }


    }

    override fun onStart() {
        super.onStart()

        disposableOperands = publishSubjectOperands
            .subscribeOn(Schedulers.io())
            .filter{ binding.formula.text.toString().length < 15 }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                str ->
                if (sequence.isNotEmpty() && flagOP){
                    sequence.clear()
                    binding.formula.text = ""
                }
                sequence.append(str)
                binding.formula.text = sequence
                flagOP = false
            }

        disposableOperator = publishSubjectOperator
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                str ->
                if (sequence.isNotEmpty()){
                    if (!flagOP){
                        flagOP = true
                        sequence.append(str)
                        binding.formula.text = ""
                    }
                    else{

                    }
                    sequence.append(str)
                    binding.formula.text = sequence
                    flagOP = true
                }
            }



        // 버튼을 누르면 문자열을 스트림으로 보낸다
        // 1) operands
        //      1) if operator 입력 기록 존재 : 화면 초기화 후 스트림 출력
        //      2) else 스트림 출력
        // 2) operator
        //      1) if 계산 가능 : 계산하여 출력 (화면 갱신)
        //          1-2) if else 스트림 출력
        // 3) etc



    }

    override fun onDestroy() {
        super.onDestroy()

        if (disposableOperands?.isDisposed != true)
            disposableOperands?.dispose()

        if (disposableOperator?.isDisposed != true)
            disposableOperator?.dispose()

    }

}