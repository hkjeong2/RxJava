package com.example.calculator.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.Exception
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val publishSubjectCalculator = PublishSubject.create<String>()
    private var disposableCalculator : Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListsener()

    }

    private fun initClickListsener(){

        binding.bt0.setOnClickListener {
            publishSubjectCalculator.onNext("0")
        }
        binding.bt1.setOnClickListener {
            publishSubjectCalculator.onNext("1")
        }
        binding.bt2.setOnClickListener {
            publishSubjectCalculator.onNext("2")
        }
        binding.bt3.setOnClickListener {
            publishSubjectCalculator.onNext("3")
        }
        binding.bt4.setOnClickListener {
            publishSubjectCalculator.onNext("4")
        }
        binding.bt5.setOnClickListener {
            publishSubjectCalculator.onNext("5")
        }
        binding.bt6.setOnClickListener {
            publishSubjectCalculator.onNext("6")
        }
        binding.bt7.setOnClickListener {
            publishSubjectCalculator.onNext("7")
        }
        binding.bt8.setOnClickListener {
            publishSubjectCalculator.onNext("8")
        }
        binding.bt9.setOnClickListener {
            publishSubjectCalculator.onNext("9")
        }
        binding.add.setOnClickListener {
            publishSubjectCalculator.onNext("+")
        }
        binding.subtract.setOnClickListener {
            publishSubjectCalculator.onNext("-")
        }
        binding.multiply.setOnClickListener {
            publishSubjectCalculator.onNext("*")
        }
        binding.divide.setOnClickListener {
            publishSubjectCalculator.onNext("/")
        }
        binding.signChange.setOnClickListener {
            publishSubjectCalculator.onNext("neg")
        }
        binding.percentage.setOnClickListener {
            publishSubjectCalculator.onNext("perc")
        }
        binding.dot.setOnClickListener {
            publishSubjectCalculator.onNext(".")
        }
        binding.equals.setOnClickListener {
            publishSubjectCalculator.onNext("=")
        }
        binding.clear.setOnClickListener {
            publishSubjectCalculator.onNext("C")
        }

    }

    private fun priority(operator : Char) : Int{
        return when (operator) {
            '+' -> 1
            '-' -> 1
            '*' -> 2
            '/' -> 2
            else -> -1
        }
    }

    private fun calculateTwoOperands(n1 : Double, n2 : Double, op : String) : Double{
        var result = 0.0

        when (op) {
            "+" -> result = n1 + n2
            "-" -> result = n1 - n2
            "*" -> result = n1 * n2
            "/" -> result = n1 / n2
        }

        return result
    }

    private fun getStack(input : String) : Stack<String>{
        // 입력값을 연산자 기준으로 나눠 계산하기 위해 스택으로 변경
        val stack = Stack<String>()

        val item = StringBuilder()
        for(i in input.indices){
            if (i == 0 && input[0] =='-'){
                item.append(input[i])
            }
            else{
                if (isOperator(input[i].toString())){
                    // operator 일 시 이전까지 저장한 값(operands)과 현재 operator 를 스택에 저장
                    stack.add(item.toString())
                    stack.add(input[i].toString())
                    item.clear()
                }
                else{
                    item.append(input[i])
                }
            }
        }
        if (item.isNotEmpty())
            stack.add(item.toString())

        return stack
    }

    private fun giveEqualSignResult(input : String) : String{
        if (isLastCharEqualSign(input))
            return "0"
        return if (!isLastCharOperator(input)){
            val stack = getStack(input)
            while (stack.size >= 3)
                calculateWithStack(stack)
            stack.joinToString("") + "="
        } else
            "0="
    }

    private fun isLastCharOperator(input : String) : Boolean{
        return isOperator(input[input.length - 1].toString())
    }

    private fun isLastCharEqualSign(input : String) : Boolean{
        return input[input.length - 1] == '='
    }

    private fun isOperator(input : String) : Boolean{
        return input == "+" || input == "-" || input == "*" || input == "/"
    }

    private fun changeToCalculatableFormat(input : String) : String{
        if (isLastCharEqualSign(input))
            return "0"
        if (isLastCharOperator(input))
            return input.substring(0, input.length - 1)
        return input
    }

    private fun checkDecimal(input : String) : String{
        var lastSymbol = getStack(input).peek()
        Log.d("value11 : ", lastSymbol)

        // 소수점 포함 케이스
        if (lastSymbol.replace(".","") != lastSymbol)
            return input

        if (isLastCharEqualSign(lastSymbol) || isOperator(lastSymbol))
            return input + "0."

        if (lastSymbol[0] == '0' && lastSymbol.length >= 2)
            return lastSymbol.substring(1) + "." // 0 으로 시작하는 케이스 제거

        return input + "."
    }


    private fun calculateWithStack(stack : Stack<String>){
        val n2 = stack.pop().toDouble()
        val lastOp = stack.pop()
        val n1 = stack.pop().toDouble()
        val result = calculateTwoOperands(n1, n2, lastOp)

        if (isConvertibleToInt(result))
            stack.push(result.toInt().toString())
        else
            stack.push(result.toString())
    }

    private fun isConvertibleToInt(value : Double) : Boolean{
        try{
            val valueInt = value.toInt()
            return value == valueInt.toDouble()
        }catch(e : Exception){
            return false
        }
    }

    private fun calculate(stack : Stack<String>, currentOp : String) : String{
        if (stack.size >= 3){   // 계산 가능한 포맷일 경우 (피연산자 2개, 연산자 1개)

            if (currentOp == "+" || currentOp == "-"){
                calculateWithStack(stack)
                calculate(stack, currentOp)
            }
            else if (currentOp == "*" || currentOp == "/"){
                if (priority(currentOp[0]) <= priority(stack[stack.size - 2][0])){
                    calculateWithStack(stack)
                }
            }
        }

        return stack.joinToString("")
    }

    private fun refineResult(input : String) : String{
        if (input[input.length - 1] == '.')
            return input

        val result = input.toDouble().toString()
        val resultToken = result.split(".")

        // 정수 형변환 가능 시
        if (result.toDouble() - resultToken[0].toDouble() == 0.0)
            return resultToken[0]

        // 유한 소수
        if (result == String.format("%.5f", result.toDouble()).toDouble().toString())
            return result

        // 무한 소수
        return String.format("%.5f", result.toDouble())
    }

    private fun giveLastNumberInStackAsResult(input : String) : String{
        var temp = input

        if (isLastCharOperator(input) || isLastCharEqualSign(input))
            temp = input.substring(0, input.length - 1)

        return refineResult(getStack(temp).peek())
    }

    override fun onStart() {
        super.onStart()

        disposableCalculator = publishSubjectCalculator
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .scan("0") { accumulatedValue : String, currentValue : String -> // accumulatedValue = 누적 입력값 , currentValue = 새 입력값

                Log.d("value1 : ", accumulatedValue)
                Log.d("value2 : ", currentValue)
                // "=" 직후 op 는 계산
                // "=" 직후 dot
                when (currentValue) {
                    "+", "-", "*", "/" -> {
                        // 연산자일 경우 계산 및 결괏값 update
                        val result = calculate(getStack(changeToCalculatableFormat(accumulatedValue)), currentValue)
                        result + currentValue
                    }
                    "C" -> {
                        // 초기화
                        "0"
                    }
                    "=" -> {
                        giveEqualSignResult(accumulatedValue)
                    }
                    "." -> {
                        checkDecimal(accumulatedValue)
                    }
                    else -> {   // 피연산자
                        if(isLastCharEqualSign(accumulatedValue))
                            currentValue
                        else // 연산자일 경우 그대로 출력
                            accumulatedValue + currentValue
                    }
                }

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ input ->
                Log.d("입력값", input)
                binding.result.text = giveLastNumberInStackAsResult(input)
            }


    }

    override fun onDestroy() {
        super.onDestroy()

        if (disposableCalculator?.isDisposed != true)
            disposableCalculator?.dispose()

    }

}

