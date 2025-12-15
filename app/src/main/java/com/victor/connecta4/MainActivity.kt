package com.victor.connecta4

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    companion object {
        private const val ROWS = 6
        private const val COLS = 7
        private const val EMPTY = 0
        private const val RED = 1
        private const val YELLOW = 2
        private const val CHIP_SIZE = 150
    }

    private lateinit var gameBoard: TableLayout
    private lateinit var buttonsContainer: LinearLayout
    private val board = Array(ROWS) { IntArray(COLS) }
    private var currentPlayer = RED
    private lateinit var chips: Array<Array<ImageView>>
    private lateinit var buttons: Array<Button>
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameBoard = findViewById(R.id.gameBoard)
        buttonsContainer = findViewById(R.id.buttonsContainer)

        initializeBoard()
        createButtons()
        
        // Animar entrada del tablero
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        gameBoard.startAnimation(fadeInAnimation)
    }

    private fun initializeBoard() {
        // Inicializar el array de chips
        chips = Array(ROWS) { Array(COLS) { null as ImageView? } as Array<ImageView> }
        
        // Crear la tabla con ImageViews
        for (row in 0 until ROWS) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            for (col in 0 until COLS) {
                val chip = ImageView(this)
                chip.layoutParams = TableRow.LayoutParams(0, CHIP_SIZE, 1f)
                chip.setImageResource(R.drawable.chip_empty)
                chip.scaleType = ImageView.ScaleType.FIT_CENTER
                tableRow.addView(chip)
                chips[row][col] = chip
            }

            gameBoard.addView(tableRow)
        }
    }

    private fun createButtons() {
        buttons = Array(COLS) { Button(this) }
        for (col in 0 until COLS) {
            val button = Button(this)
            button.text = "↓"
            button.layoutParams = LinearLayout.LayoutParams(0, 100, 1f)
            button.setBackgroundResource(R.drawable.rounded_button)
            button.setTextColor(resources.getColor(android.R.color.white, null))
            button.textSize = 24f
            button.setOnClickListener {
                if (!gameOver) {
                    dropChip(col)
                    // Animar el botón
                    val popInAnimation = AnimationUtils.loadAnimation(this, R.anim.chip_drop)
                    button.startAnimation(popInAnimation)
                }
            }
            buttons[col] = button
            buttonsContainer.addView(button)
        }
    }

    private fun dropChip(col: Int) {
        // Encontrar la fila más baja vacía en esta columna
        for (row in ROWS - 1 downTo 0) {
            if (board[row][col] == EMPTY) {
                board[row][col] = currentPlayer
                updateChipDisplay(row, col)
                
                // Animar la ficha
                val dropAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_in)
                chips[row][col].startAnimation(dropAnimation)
                
                // Verificar si hay ganador
                if (checkWin(row, col)) {
                    gameOver = true
                    showWinnerDialog()
                    return
                }
                
                switchPlayer()
                return
            }
        }
        // Si llegamos aquí, la columna está llena
        Toast.makeText(this, "¡Columna llena!", Toast.LENGTH_SHORT).show()
    }

    private fun updateChipDisplay(row: Int, col: Int) {
        val drawable = when (board[row][col]) {
            RED -> R.drawable.chip_red
            YELLOW -> R.drawable.chip_yellow
            else -> R.drawable.chip_empty
        }
        chips[row][col].setImageResource(drawable)
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == RED) YELLOW else RED
    }

    private fun checkWin(row: Int, col: Int): Boolean {
        val player = board[row][col]
        
        // Verificar horizontal
        if (checkDirection(row, col, 0, 1, player)) return true
        
        // Verificar vertical
        if (checkDirection(row, col, 1, 0, player)) return true
        
        // Verificar diagonal (arriba-izq a abajo-der)
        if (checkDirection(row, col, 1, 1, player)) return true
        
        // Verificar diagonal (arriba-der a abajo-izq)
        if (checkDirection(row, col, 1, -1, player)) return true
        
        return false
    }

    private fun checkDirection(row: Int, col: Int, deltaRow: Int, deltaCol: Int, player: Int): Boolean {
        var count = 1
        
        // Contar hacia un lado
        var r = row + deltaRow
        var c = col + deltaCol
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++
            r += deltaRow
            c += deltaCol
        }
        
        // Contar hacia el otro lado
        r = row - deltaRow
        c = col - deltaCol
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++
            r -= deltaRow
            c -= deltaCol
        }
        
        return count >= 4
    }

    private fun showWinnerDialog() {
        val playerName = if (currentPlayer == RED) "Rojo" else "Amarillo"
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_winner, null)
        val winnerTitle = dialogView.findViewById<android.widget.TextView>(R.id.winnerTitle)
        val btnNewGame = dialogView.findViewById<Button>(R.id.btnNewGame)
        
        winnerTitle.text = "¡El jugador $playerName ha ganado!"
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnNewGame.setOnClickListener {
            resetGame()
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun resetGame() {
        // Limpiar el tablero
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                board[row][col] = EMPTY
                updateChipDisplay(row, col)
            }
        }
        currentPlayer = RED
        gameOver = false
    }
}