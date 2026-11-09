package com.associacaocamila.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.Workshop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WorkshopReportPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private val COLOR_PRIMARY = Color.parseColor("#00A669") // CamilaGreen
    private val COLOR_PRIMARY_DARK = Color.parseColor("#007A4D") // CamilaGreenDark
    private val COLOR_GRAY = Color.parseColor("#7C7C7C")
    private val COLOR_ZEBRA = Color.parseColor("#F4FBF7")
    private val COLOR_BORDER = Color.parseColor("#E0E0E0")

    /**
     * Gera o relatório de frequência em PDF para a oficina selecionada.
     */
    suspend fun generateAttendancePdf(
        context: Context,
        workshop: Workshop,
        records: List<AttendanceRecord>,
        studentsMap: Map<String, String>,
        teacherName: String = "Professor(a)",
        scheduleInfo: String = "Encontros Semanais",
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Cabeçalho Institucional
        paint.color = COLOR_PRIMARY
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 70f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 18f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Associação Camila", 28f, 32f, textPaint)

        textPaint.textSize = 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Relatório de Frequência da Oficina", 28f, 54f, textPaint)

        // Faixa com Data de Emissão à direita no topo
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        val emitDateStr = "Emissão: ${dateFormat.format(Date())}"
        textPaint.textSize = 10f
        val dateWidth = textPaint.measureText(emitDateStr)
        canvas.drawText(emitDateStr, PAGE_WIDTH - 28f - dateWidth, 42f, textPaint)

        // 2. Bloco de Metadados da Oficina
        var currentY = 100f
        paint.color = Color.parseColor("#F8F9FA")
        canvas.drawRoundRect(24f, currentY, PAGE_WIDTH - 24f, currentY + 74f, 8f, 8f, paint)

        textPaint.color = COLOR_PRIMARY_DARK
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Oficina: ${workshop.name}", 38f, currentY + 24f, textPaint)

        textPaint.color = Color.BLACK
        textPaint.textSize = 10.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Instrutor(a): $teacherName", 38f, currentY + 44f, textPaint)
        canvas.drawText("Horários/Dias: $scheduleInfo", 38f, currentY + 62f, textPaint)

        val totalClasses = records.size
        val totalClassesStr = "Total de Aulas Registradas: $totalClasses"
        val totalClassesWidth = textPaint.measureText(totalClassesStr)
        canvas.drawText(totalClassesStr, PAGE_WIDTH - 38f - totalClassesWidth, currentY + 44f, textPaint)

        val totalStudentsStr = "Participantes Matriculados: ${workshop.enrolledStudentIds.size}"
        val totalStudentsWidth = textPaint.measureText(totalStudentsStr)
        canvas.drawText(totalStudentsStr, PAGE_WIDTH - 38f - totalStudentsWidth, currentY + 62f, textPaint)

        currentY += 95f

        // 3. Tabela de Frequência
        val startX = 24f
        val endX = PAGE_WIDTH - 24f
        val tableWidth = endX - startX

        // Cabeçalho da Tabela
        val headerHeight = 28f
        paint.color = COLOR_PRIMARY
        canvas.drawRoundRect(startX, currentY, endX, currentY + headerHeight, 4f, 4f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 10.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col1X = startX + 10f       // Participante
        val col2X = startX + 260f      // Total Aulas
        val col3X = startX + 335f      // Presenças
        val col4X = startX + 410f      // Faltas
        val col5X = startX + 475f      // % Presença

        canvas.drawText("Participante", col1X, currentY + 18f, textPaint)
        canvas.drawText("Aulas", col2X, currentY + 18f, textPaint)
        canvas.drawText("Presenças", col3X, currentY + 18f, textPaint)
        canvas.drawText("Faltas", col4X, currentY + 18f, textPaint)
        canvas.drawText("% Presença", col5X, currentY + 18f, textPaint)

        currentY += headerHeight

        // Linhas de Alunos
        val rowHeight = 24f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 10f

        val enrolledIds = workshop.enrolledStudentIds
        if (enrolledIds.isEmpty()) {
            paint.color = COLOR_ZEBRA
            canvas.drawRect(startX, currentY, endX, currentY + rowHeight + 10f, paint)
            textPaint.color = COLOR_GRAY
            canvas.drawText("Nenhum participante matriculado nesta oficina.", startX + 16f, currentY + 22f, textPaint)
            currentY += rowHeight + 10f
        } else {
            enrolledIds.forEachIndexed { index, studentId ->
                val studentName = studentsMap[studentId] ?: "Aluno ($studentId)"

                // Cálculo de presenças e faltas para o participante
                var presenceCount = 0
                var absenceCount = 0

                records.forEach { record ->
                    val isPresent = record.attendanceMap[studentId]
                    if (isPresent == true) {
                        presenceCount++
                    } else if (isPresent == false) {
                        absenceCount++
                    }
                }

                val totalRecorded = presenceCount + absenceCount
                val presencePercent = if (totalRecorded > 0) {
                    (presenceCount.toFloat() / totalRecorded * 100).toInt()
                } else {
                    0
                }

                // Fundo zebrado
                if (index % 2 == 1) {
                    paint.color = COLOR_ZEBRA
                    canvas.drawRect(startX, currentY, endX, currentY + rowHeight, paint)
                }

                // Linha de divisão sutil
                paint.color = COLOR_BORDER
                canvas.drawLine(startX, currentY + rowHeight, endX, currentY + rowHeight, paint)

                textPaint.color = Color.BLACK
                val truncatedName = if (studentName.length > 34) studentName.take(31) + "..." else studentName
                canvas.drawText(truncatedName, col1X, currentY + 16f, textPaint)

                canvas.drawText(totalRecorded.toString(), col2X + 6f, currentY + 16f, textPaint)
                canvas.drawText(presenceCount.toString(), col3X + 12f, currentY + 16f, textPaint)
                canvas.drawText(absenceCount.toString(), col4X + 8f, currentY + 16f, textPaint)

                // Cor percentual de presença
                if (presencePercent >= 75) {
                    textPaint.color = COLOR_PRIMARY_DARK
                } else {
                    textPaint.color = Color.parseColor("#D32F2F")
                }
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("$presencePercent%", col5X + 10f, currentY + 16f, textPaint)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                currentY += rowHeight
            }
        }

        // 4. Rodapé Institucional e Assinatura
        val footerY = PAGE_HEIGHT - 90f

        // Linha para assinatura
        paint.color = COLOR_GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(PAGE_WIDTH / 2f - 120f, footerY, PAGE_WIDTH / 2f + 120f, footerY, paint)

        textPaint.color = COLOR_GRAY
        textPaint.textSize = 9.5f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Assinatura do(a) Coordenador(a) / Professor(a)", PAGE_WIDTH / 2f, footerY + 14f, textPaint)

        // Lema Institucional
        textPaint.color = COLOR_PRIMARY_DARK
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EM DEFESA E VALORIZAÇÃO DA VIDA", PAGE_WIDTH / 2f, PAGE_HEIGHT - 32f, textPaint)

        textPaint.textAlign = Paint.Align.LEFT
        pdfDocument.finishPage(page)

        // Salvar em context.cacheDir
        val outputFile = File(context.cacheDir, "relatorio_${workshop.id}.pdf")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        FileOutputStream(outputFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        outputFile
    }

    /**
     * Dispara Intent para compartilhar o PDF gerado via apps externos.
     */
    fun sharePdf(context: Context, file: File) {
        runCatching {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório de Frequência - Associação Camila")
                putExtra(Intent.EXTRA_TEXT, "Segue anexo o Relatório de Frequência oficial da oficina.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Relatório de Frequência").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.onFailure { ex ->
            Toast.makeText(
                context,
                "Não foi possível compartilhar o arquivo: ${ex.localizedMessage}",
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
