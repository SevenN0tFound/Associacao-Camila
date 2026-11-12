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

    private val COLOR_PRIMARY = Color.parseColor("#00A669")
    private val COLOR_PRIMARY_DARK = Color.parseColor("#007A4D")
    private val COLOR_GRAY = Color.parseColor("#7C7C7C")
    private val COLOR_ZEBRA = Color.parseColor("#F4FBF7")
    private val COLOR_BORDER = Color.parseColor("#E0E0E0")

    // Cores dos Badges de Situação
    private val COLOR_PRESENT_BG = Color.parseColor("#E8F5E9")
    private val COLOR_PRESENT_TEXT = Color.parseColor("#2E7D32")
    private val COLOR_ABSENT_BG = Color.parseColor("#FFEBEE")
    private val COLOR_ABSENT_TEXT = Color.parseColor("#C62828")

    /**
     * Gera o relatório de frequência em PDF para a oficina selecionada com suporte a período customizado,
     * resumo consolidado de presenças/faltas e badges explícitos para cada aluno.
     */
    suspend fun generateAttendancePdf(
        context: Context,
        workshop: Workshop,
        records: List<AttendanceRecord>,
        studentsMap: Map<String, String>,
        startDateMillis: Long? = null,
        endDateMillis: Long? = null,
        teacherName: String = "Professor(a)",
        scheduleInfo: String = "Encontros Semanais",
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Identificação completa de participantes (matriculados + histórico)
        val allStudentIds = (workshop.enrolledStudentIds + studentsMap.keys + records.flatMap { it.attendanceMap.keys })
            .distinct()
            .filter { it.isNotBlank() }

        // 2. Cálculo do Resumo Consolidado (Total de Registros, Presenças, Faltas e Assiduidade)
        var globalPresences = 0
        var globalAbsences = 0

        records.forEach { record ->
            val evaluatedIds = (record.attendanceMap.keys + record.statusMap.keys).distinct().filter { it.isNotBlank() }
            evaluatedIds.forEach { sId ->
                val isPres = record.attendanceMap[sId]
                if (isPres == true) {
                    globalPresences++
                } else if (isPres == false) {
                    globalAbsences++
                } else {
                    val status = record.statusMap[sId]
                    if (status?.equals("PRESENTE", ignoreCase = true) == true) {
                        globalPresences++
                    } else if (status?.equals("FALTA", ignoreCase = true) == true) {
                        globalAbsences++
                    }
                }
            }
        }

        val globalTotalRecords = globalPresences + globalAbsences
        val globalAssiduidade = if (globalTotalRecords > 0) {
            (globalPresences.toFloat() / globalTotalRecords * 100).toInt()
        } else {
            0
        }

        val startX = 24f
        val endX = PAGE_WIDTH - 24f
        val footerAreaHeight = 110f

        // Funções auxiliares para desenho da tabela
        fun drawTableHeader(canvas: Canvas, y: Float): Float {
            val headerHeight = 24f
            paint.color = COLOR_PRIMARY
            canvas.drawRoundRect(startX, y, endX, y + headerHeight, 4f, 4f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 9.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textAlign = Paint.Align.LEFT

            canvas.drawText("Participante", startX + 8f, y + 16f, textPaint)
            canvas.drawText("Aulas", startX + 225f, y + 16f, textPaint)
            canvas.drawText("Presenças", startX + 275f, y + 16f, textPaint)
            canvas.drawText("Faltas", startX + 340f, y + 16f, textPaint)
            canvas.drawText("Assiduidade", startX + 395f, y + 16f, textPaint)
            canvas.drawText("Situação", startX + 475f, y + 16f, textPaint)

            return y + headerHeight
        }

        fun drawFooter(canvas: Canvas) {
            val footerSummaryY = PAGE_HEIGHT - 96f
            paint.color = Color.parseColor("#ECEFF1")
            canvas.drawRoundRect(startX, footerSummaryY, endX, footerSummaryY + 22f, 4f, 4f, paint)

            textPaint.textSize = 8.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.parseColor("#37474F")
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "Resumo Geral: Total Registros: $globalTotalRecords  |  Presenças: $globalPresences  |  Faltas: $globalAbsences  |  Assiduidade Geral: $globalAssiduidade%",
                PAGE_WIDTH / 2f,
                footerSummaryY + 15f,
                textPaint
            )

            // Linha para assinatura
            val signatureLineY = PAGE_HEIGHT - 54f
            paint.color = COLOR_GRAY
            paint.strokeWidth = 1f
            canvas.drawLine(PAGE_WIDTH / 2f - 110f, signatureLineY, PAGE_WIDTH / 2f + 110f, signatureLineY, paint)

            textPaint.color = COLOR_GRAY
            textPaint.textSize = 8.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Assinatura do(a) Coordenador(a) / Instrutor(a)", PAGE_WIDTH / 2f, signatureLineY + 12f, textPaint)

            textPaint.color = COLOR_PRIMARY_DARK
            textPaint.textSize = 9f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("ASSOCIAÇÃO CAMILA • EM DEFESA E VALORIZAÇÃO DA VIDA", PAGE_WIDTH / 2f, PAGE_HEIGHT - 22f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
        }

        var currentPageNumber = 1
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // 3. Cabeçalho Institucional Página 1
        paint.color = COLOR_PRIMARY
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 64f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 17f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Associação Camila", 28f, 30f, textPaint)

        textPaint.textSize = 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Relatório Oficial de Frequência da Oficina", 28f, 48f, textPaint)

        val emitDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        val emitDateStr = "Emissão: ${emitDateFormat.format(Date())}"
        textPaint.textSize = 9.5f
        val dateWidth = textPaint.measureText(emitDateStr)
        canvas.drawText(emitDateStr, PAGE_WIDTH - 28f - dateWidth, 40f, textPaint)

        // 4. Bloco de Metadados da Oficina e Período
        var currentY = 76f
        val blockHeight = 68f
        paint.color = Color.parseColor("#F8F9FA")
        canvas.drawRoundRect(startX, currentY, endX, currentY + blockHeight, 6f, 6f, paint)

        textPaint.color = COLOR_PRIMARY_DARK
        textPaint.textSize = 12.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Oficina: ${workshop.name}", startX + 12f, currentY + 18f, textPaint)

        val periodDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
        val periodText = if (startDateMillis != null && endDateMillis != null) {
            "Período: ${periodDateFormat.format(Date(startDateMillis))} a ${periodDateFormat.format(Date(endDateMillis))}"
        } else {
            "Período: Histórico Completo"
        }

        textPaint.color = Color.BLACK
        textPaint.textSize = 9.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Instrutor(a): $teacherName", startX + 12f, currentY + 34f, textPaint)
        canvas.drawText(periodText, startX + 12f, currentY + 48f, textPaint)
        canvas.drawText("Horários/Dias: $scheduleInfo", startX + 12f, currentY + 62f, textPaint)

        val totalClasses = records.size
        val totalClassesStr = "Aulas Realizadas no Período: $totalClasses"
        val totalClassesWidth = textPaint.measureText(totalClassesStr)
        canvas.drawText(totalClassesStr, endX - 12f - totalClassesWidth, currentY + 34f, textPaint)

        val totalStudentsStr = "Alunos Identificados: ${allStudentIds.size}"
        val totalStudentsWidth = textPaint.measureText(totalStudentsStr)
        canvas.drawText(totalStudentsStr, endX - 12f - totalStudentsWidth, currentY + 48f, textPaint)

        currentY += blockHeight + 10f

        // 5. Resumo Consolidado no Topo (4 Cards de Métricas: Total Registros, Presenças, Faltas, Assiduidade)
        val kpiCardHeight = 44f
        val totalKpiWidth = endX - startX
        val kpiGap = 8f
        val kpiCardWidth = (totalKpiWidth - (kpiGap * 3)) / 4f

        fun drawKpiCard(x: Float, label: String, value: String, bgColor: Int, textColor: Int) {
            paint.color = bgColor
            canvas.drawRoundRect(x, currentY, x + kpiCardWidth, currentY + kpiCardHeight, 6f, 6f, paint)

            paint.color = COLOR_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(x, currentY, x + kpiCardWidth, currentY + kpiCardHeight, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            textPaint.textSize = 7.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.parseColor("#546E7A")
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, x + (kpiCardWidth / 2f), currentY + 16f, textPaint)

            textPaint.textSize = 14f
            textPaint.color = textColor
            canvas.drawText(value, x + (kpiCardWidth / 2f), currentY + 35f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }

        drawKpiCard(
            x = startX,
            label = "TOTAL DE REGISTROS",
            value = globalTotalRecords.toString(),
            bgColor = Color.parseColor("#F1F5F9"),
            textColor = Color.parseColor("#1E293B"),
        )
        drawKpiCard(
            x = startX + kpiCardWidth + kpiGap,
            label = "TOTAL DE PRESENÇAS",
            value = globalPresences.toString(),
            bgColor = COLOR_PRESENT_BG,
            textColor = COLOR_PRESENT_TEXT,
        )
        drawKpiCard(
            x = startX + (kpiCardWidth + kpiGap) * 2,
            label = "TOTAL DE FALTAS",
            value = globalAbsences.toString(),
            bgColor = COLOR_ABSENT_BG,
            textColor = COLOR_ABSENT_TEXT,
        )
        drawKpiCard(
            x = startX + (kpiCardWidth + kpiGap) * 3,
            label = "TAXA DE ASSIDUIDADE",
            value = "$globalAssiduidade%",
            bgColor = if (globalAssiduidade >= 75) Color.parseColor("#E0F2F1") else Color.parseColor("#FFF3E0"),
            textColor = if (globalAssiduidade >= 75) COLOR_PRIMARY_DARK else Color.parseColor("#E65100"),
        )

        currentY += kpiCardHeight + 14f

        // 6. Cabeçalho da Tabela
        currentY = drawTableHeader(canvas, currentY)

        // 7. Linhas dos Alunos com Situação Explícita ("PRESENTE" / "FALTA")
        val rowHeight = 22f

        if (allStudentIds.isEmpty()) {
            paint.color = COLOR_ZEBRA
            canvas.drawRect(startX, currentY, endX, currentY + rowHeight + 10f, paint)
            textPaint.color = COLOR_GRAY
            textPaint.textSize = 9.5f
            canvas.drawText("Nenhum participante ou registro de frequência localizado.", startX + 12f, currentY + 18f, textPaint)
            currentY += rowHeight + 10f
        } else {
            allStudentIds.forEachIndexed { index, studentId ->
                // Quebra de página caso ultrapasse o limite superior da área de rodapé
                if (currentY + rowHeight > PAGE_HEIGHT - footerAreaHeight) {
                    pdfDocument.finishPage(page)
                    currentPageNumber++
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Cabeçalho simplificado de continuação
                    paint.color = COLOR_PRIMARY
                    canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 34f, paint)
                    textPaint.color = Color.WHITE
                    textPaint.textSize = 10.5f
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("Associação Camila • ${workshop.name} (Página $currentPageNumber)", startX, 22f, textPaint)

                    currentY = drawTableHeader(canvas, 44f)
                }

                val studentName = studentsMap[studentId] ?: "Aluno ($studentId)"

                var presenceCount = 0
                var absenceCount = 0

                records.forEach { record ->
                    val isPresent = record.attendanceMap[studentId]
                    if (isPresent == true) {
                        presenceCount++
                    } else if (isPresent == false) {
                        absenceCount++
                    } else {
                        val status = record.statusMap[studentId]
                        if (status?.equals("PRESENTE", ignoreCase = true) == true) {
                            presenceCount++
                        } else if (status?.equals("FALTA", ignoreCase = true) == true) {
                            absenceCount++
                        }
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

                paint.color = COLOR_BORDER
                canvas.drawLine(startX, currentY + rowHeight, endX, currentY + rowHeight, paint)

                textPaint.color = Color.BLACK
                textPaint.textSize = 9.5f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textAlign = Paint.Align.LEFT

                val truncatedName = if (studentName.length > 30) studentName.take(28) + "..." else studentName
                canvas.drawText(truncatedName, startX + 8f, currentY + 15f, textPaint)

                canvas.drawText(totalRecorded.toString(), startX + 230f, currentY + 15f, textPaint)
                canvas.drawText(presenceCount.toString(), startX + 285f, currentY + 15f, textPaint)
                canvas.drawText(absenceCount.toString(), startX + 348f, currentY + 15f, textPaint)

                if (totalRecorded > 0) {
                    textPaint.color = if (presencePercent >= 75) COLOR_PRIMARY_DARK else Color.parseColor("#D32F2F")
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("$presencePercent%", startX + 402f, currentY + 15f, textPaint)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                } else {
                    textPaint.color = COLOR_GRAY
                    canvas.drawText("-", startX + 408f, currentY + 15f, textPaint)
                }

                // Badge de Situação Explícita ("PRESENTE" em verde / "FALTA" em vermelho)
                val badgeX = startX + 465f
                val badgeWidth = 68f
                val badgeTop = currentY + 3f
                val badgeBottom = currentY + rowHeight - 3f

                val isPresentSituation = if (records.size == 1) {
                    presenceCount > 0
                } else {
                    presenceCount >= absenceCount && totalRecorded > 0
                }

                if (totalRecorded == 0) {
                    paint.color = Color.parseColor("#EEEEEE")
                    canvas.drawRoundRect(badgeX, badgeTop, badgeX + badgeWidth, badgeBottom, 4f, 4f, paint)

                    textPaint.textSize = 8f
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.color = Color.parseColor("#757575")
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("S/ REGISTRO", badgeX + (badgeWidth / 2f), currentY + 14.5f, textPaint)
                } else if (isPresentSituation) {
                    paint.color = COLOR_PRESENT_BG
                    canvas.drawRoundRect(badgeX, badgeTop, badgeX + badgeWidth, badgeBottom, 4f, 4f, paint)

                    textPaint.textSize = 8.5f
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.color = COLOR_PRESENT_TEXT
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("PRESENTE", badgeX + (badgeWidth / 2f), currentY + 14.5f, textPaint)
                } else {
                    paint.color = COLOR_ABSENT_BG
                    canvas.drawRoundRect(badgeX, badgeTop, badgeX + badgeWidth, badgeBottom, 4f, 4f, paint)

                    textPaint.textSize = 8.5f
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.color = COLOR_ABSENT_TEXT
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("FALTA", badgeX + (badgeWidth / 2f), currentY + 14.5f, textPaint)
                }

                textPaint.textAlign = Paint.Align.LEFT
                currentY += rowHeight
            }
        }

        // 8. Rodapé Institucional com Resumo e Assinatura
        drawFooter(canvas)
        pdfDocument.finishPage(page)

        // Salvar em context.cacheDir
        val outputFile = File(context.cacheDir, "relatorio_frequencia_${workshop.id}.pdf")
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
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo o Relatório de Frequência oficial da oficina da Associação Camila.")
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
