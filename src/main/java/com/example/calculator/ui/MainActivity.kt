package com.example.calculator.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val publishSubjectOperands = PublishSubject.create<String>()
    private val publishSubjectOperator = PublishSubject.create<String>()
    private val publishSubjectExtraOp = PublishSubject.create<String>()
    private var disposableOperands : Disposable? = null
    private var disposableOperator : Disposable? = null
    private var disposableExtraOp : Disposable? = null
    private var flagOp : Boolean = false // 마지막 입력값이 operator 인지
    private var isOpFirst : Boolean = true // sequence 의 첫 operator 인지 (그 외 경우에만 계산)
    private var isCalculated : Boolean = false // sequence 의 첫 operator 인지 (그 외 경우에만 계산)
    private var sequence = StringBuilder("0")   // 계산식을 위한 일련의 입력 정보 저장
    private var numberOnView = StringBuilder("0")   // 화면상에 보일 숫자

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

        binding.calculate.setOnClickListener {
            publishSubjectExtraOp.onNext("=")
        }
        binding.clear.setOnClickListener {
            publishSubjectExtraOp.onNext("C")
        }

    }

    private fun opOrder(op : Char) : Int{
        return when (op) {
            '+' -> 1
            '-' -> 1
            '*' -> 2
            '/' -> 2
            else -> -1
        }
    }

    private fun init(){
        flagOp = false // 마지막 입력값이 operator 인지
        isOpFirst = true // sequence 의 첫 operator 인지 (그 외 경우에만 계산)
        isCalculated = false
        sequence = StringBuilder("0")   // 계산식을 위한 일련의 입력 정보 저장
        numberOnView = StringBuilder("0")   // 화면상에 보일 숫자
    }

    private fun calculate(sb : StringBuilder) : String{

        val operation = arrayOf('+', '-', '*', '/')
        val postfix = ArrayList<String>()
        val stackOP = Stack<Char>()
        val stackCal = Stack<String>()
        var num = ""

        for (i in sb.indices){
            var checkOP = false
            for (j in operation.indices){
                if (sb[i] == operation[j]){
                    checkOP = true

                    if (num != ""){
                        postfix.add(num)
                        num = ""
                    }

                    if (stackOP.isEmpty()){
                        stackOP.push(operation[j])
                    }
                    else {
                        if (opOrder(stackOP.peek()) < opOrder(operation[j]))
                            stackOP.push(operation[j])
                        else {
                            postfix.add(stackOP.pop().toString())
                            stackOP.push(operation[j])
                        }
                    }

                }
            }
            if (!checkOP){
                num += sb[i]
            }
        }

        if (num != ""){
            postfix.add(num)
        }

        while (stackOP.isNotEmpty())
            postfix.add(stackOP.pop().toString())

        for (i in postfix.indices){
            stackCal.push(postfix[i])
            for (j in operation.indices){
                if (postfix[i][0] == operation[j]){
                    stackCal.pop()
                    val n2 : Double = stackCal.pop().toDouble()
                    var result = ""

                    val n1 : Double = stackCal.pop().toDouble()
                    if (operation[j] == '+')
                        result = (n1 + n2).toString()
                    else if (operation[j] == '-')
                        result = (n1 - n2).toString()
                    else if (operation[j] == '*')
                        result = (n1 * n2).toString()
                    else if (operation[j] == '/')
                        result = (n1 / n2).toString()

                    stackCal.push(result)
                }
            }
        }

        val result : Double = stackCal.pop().toDouble()
        Log.d("cctor", sequence.toString())
        val calResult = result.toString().split(".")

        if (calResult[1].toDouble() - 0.0 == 0.0){
            return calResult[0]
        }
        else {
            return String.format("%.5f", result)
        }

    }

    override fun onStart() {
        super.onStart()

        disposableOperands = publishSubjectOperands
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                input ->
                if (flagOp || numberOnView[0] == '0'){
                    // 첫 숫자가 0 이거나 가장 마지막으로 입력된 것이 연산자 (flagOp == true)일 경우
                    // 화면 초기화 후 피연산자 입력 및 flagOp 초기화
                    numberOnView.clear()
                    binding.formula.text = ""
                }
                else if (isCalculated){
                    sequence.clear()
                    numberOnView.clear()
                    isOpFirst = true
                    binding.formula.text = ""
                    isCalculated = false
                }
                sequence.append(input)
                numberOnView.append(input)
                binding.formula.text = numberOnView
                flagOp = false
            }

        disposableOperator = publishSubjectOperator
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                input ->
                if (isCalculated)
                    isCalculated = false
                if (flagOp){ // 마지막 입력값이 operator 이면 현재 입력된 연산자와 바꿔주기만
                    sequence[sequence.length - 1] = input[0]
                }
                else{ // 마지막 입력값이 숫자였을 경우
                    if (isOpFirst){ // the very first operator = 계산 skip
                        isOpFirst = false
                    }
                    else{ // operator 입력된 기록 있으므로 계산
                        binding.formula.text = calculate(sequence)
                    }
                    sequence.append(input)
                }
                flagOp = true
            }

        disposableExtraOp = publishSubjectExtraOp
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                input ->
                when (input) {
                    "C" -> {
                        init()
                        binding.formula.text = "0"
                    }
                    "=" -> {
                        if (flagOp){ // 마지막 입력값이 op 이면 이미 화면상에 계산 끝난 값이 옴
                            sequence = StringBuilder(numberOnView)
                            flagOp = false
                            isOpFirst = true
                            isCalculated = true // 계산 작업 완료 flag
                        }
                        else {
                            val result = calculate(sequence)
                            sequence = StringBuilder(result)   // 계산식을 위한 일련의 입력 정보 저장
                            numberOnView = StringBuilder(result)   // 화면상에 보일 숫자
                            binding.formula.text = numberOnView
                            flagOp = false
                            isOpFirst = true
                            isCalculated = true // 계산 작업 완료 flag
                        }
                    }

                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (disposableOperands?.isDisposed != true)
            disposableOperands?.dispose()

        if (disposableOperator?.isDisposed != true)
            disposableOperator?.dispose()
        
        if (disposableExtraOp?.isDisposed != true)
            disposableExtraOp?.dispose()

    }

}