package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Serviço especializado na geração de relatórios PDF de Orçamento.
 * Foca em clareza, design profissional e auditabilidade dos cálculos.
 */
public class OrcamentoPdfService {

    private static final String TAG = "OrcamentoPdfService";

    // Definição de Fontes e Estilos
    private static final Font FONT_EMPRESA = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_TITULO = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font FONT_NORMAL_BOLD = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_CABECALHO_TABELA = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_ITEM_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_TOTAL_LABEL = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_TOTAL_VALUE = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(0, 100, 0)); // Verde escuro
    private static final Font FONT_RODAPE = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);

    private final Context context;
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat dateFormat;

    public OrcamentoPdfService(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    /**
     * Gera o PDF detalhado do orçamento.
     */
    public File gerarPdf(Orcamento orcamento, Cliente cliente) throws IOException, DocumentException {
        // 1. Preparar diretório e arquivo
        File documentsDir = new File(context.getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists()) documentsDir.mkdirs();

        String nomeArquivo = "Orcamento_" + orcamento.getId() + "_" + System.currentTimeMillis() + ".pdf";
        File pdfFile = new File(documentsDir, nomeArquivo);

        // 2. Configurar Documento
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        
        // Adiciona evento para rodapé (número de páginas)
        // writer.setPageEvent(new HeaderFooterPageEvent()); // Implementar se necessário

        document.open();

        // 3. Construção do Conteúdo
        adicionarCabecalhoEmpresa(document);
        adicionarDadosOrcamentoECliente(document, orcamento, cliente);
        adicionarTabelaItens(document, orcamento);
        adicionarResumoFinanceiro(document, orcamento);
        adicionarObservacoes(document, orcamento);
        adicionarAreaAssinatura(document);
        adicionarRodapeSistema(document);

        document.close();
        return pdfFile;
    }

    private void adicionarCabecalhoEmpresa(Document document) throws DocumentException {
        // TODO: Obter dados reais da empresa via SharedPreferences ou Classe de Configuração
        String nomeEmpresa = "SUA EMPRESA AQUI"; 
        String dadosEmpresa = "CNPJ: 00.000.000/0001-00 | Tel: (00) 0000-0000\nE-mail: contato@suaempresa.com";

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        PdfPCell cellEmpresa = new PdfPCell(new Paragraph(nomeEmpresa.toUpperCase(), FONT_EMPRESA));
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        cellEmpresa.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTable.addCell(cellEmpresa);

        PdfPCell cellDados = new PdfPCell(new Paragraph(dadosEmpresa, FONT_ITEM_NORMAL));
        cellDados.setBorder(Rectangle.NO_BORDER);
        cellDados.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellDados.setPaddingBottom(10);
        headerTable.addCell(cellDados);

        // Linha divisória
        PdfPCell linha = new PdfPCell();
        linha.setBorder(Rectangle.BOTTOM);
        linha.setBorderWidth(1.5f);
        linha.setBorderColor(BaseColor.LIGHT_GRAY);
        headerTable.addCell(linha);

        document.add(headerTable);
        document.add(new Paragraph(" ")); // Espaçamento
    }

    private void adicionarDadosOrcamentoECliente(Document document, Orcamento orcamento, Cliente cliente) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1}); // 50% para cada lado

        // Lado Esquerdo: Dados do Orçamento
        PdfPCell cellOrcamento = new PdfPCell();
        cellOrcamento.setBorder(Rectangle.NO_BORDER);
        cellOrcamento.addElement(new Paragraph("ORÇAMENTO Nº " + orcamento.getId(), FONT_TITULO));
        cellOrcamento.addElement(new Paragraph("Data Emissão: " + dateFormat.format(new Date(orcamento.getDataCriacao())), FONT_ITEM_NORMAL));
        cellOrcamento.addElement(new Paragraph("Validade: 15 dias", FONT_ITEM_NORMAL)); // Exemplo estático
        table.addCell(cellOrcamento);

        // Lado Direito: Dados do Cliente
        PdfPCell cellCliente = new PdfPCell();
        cellCliente.setBorder(Rectangle.NO_BORDER);
        cellCliente.addElement(new Paragraph("CLIENTE:", FONT_TITULO));
        cellCliente.addElement(new Paragraph("Nome: " + cliente.getNome(), FONT_ITEM_NORMAL));
        cellCliente.addElement(new Paragraph("Tel: " + cliente.getTelefone(), FONT_ITEM_NORMAL));
        if (cliente.getEndereco() != null && !cliente.getEndereco().isEmpty()) {
            cellCliente.addElement(new Paragraph("End: " + cliente.getEndereco(), FONT_ITEM_NORMAL));
        }
        table.addCell(cellCliente);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void adicionarTabelaItens(Document document, Orcamento orcamento) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4f, 1f, 1.5f, 1.5f}); // Descrição maior
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Cabeçalho da Tabela
        String[] colunas = {"Descrição / Item", "Qtd", "V. Unit.", "Subtotal"};
        for (String col : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(col, FONT_CABECALHO_TABELA));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            table.addCell(cell);
        }

        // Preenchimento dos itens
        if (orcamento.getItensServicos() != null) {
            for (Orcamento.OrcamentoItemServico item : orcamento.getItensServicos()) {
                adicionarLinhaTabela(table, "Serviço: " + item.getNomeServico(), item.getQuantidade(), item.getValorUnitario(), item.getValorTotal());
            }
        }
        
        if (orcamento.getItensProdutos() != null) {
            for (Orcamento.OrcamentoItemProduto item : orcamento.getItensProdutos()) {
                adicionarLinhaTabela(table, "Produto: " + item.getNomeProduto(), item.getQuantidade(), item.getValorUnitario(), item.getValorTotal());
            }
        }

        // Se não houver itens (prevenção de erro)
        if (table.getRows().size() == 1) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("Nenhum item adicionado.", FONT_ITEM_NORMAL));
            emptyCell.setColspan(4);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(10f);
            table.addCell(emptyCell);
        }

        document.add(table);
    }

    private void adicionarLinhaTabela(PdfPTable table, String descricao, int qtd, double vUnit, double vTotal) {
        PdfPCell cellDesc = new PdfPCell(new Phrase(descricao, FONT_ITEM_NORMAL));
        cellDesc.setPadding(5f);
        table.addCell(cellDesc);

        PdfPCell cellQtd = new PdfPCell(new Phrase(String.valueOf(qtd), FONT_ITEM_NORMAL));
        cellQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellQtd.setPadding(5f);
        table.addCell(cellQtd);

        PdfPCell cellUnit = new PdfPCell(new Phrase(currencyFormat.format(vUnit), FONT_ITEM_NORMAL));
        cellUnit.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellUnit.setPadding(5f);
        table.addCell(cellUnit);

        PdfPCell cellTotal = new PdfPCell(new Phrase(currencyFormat.format(vTotal), FONT_ITEM_NORMAL));
        cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotal.setPadding(5f);
        table.addCell(cellTotal);
    }

    private void adicionarResumoFinanceiro(Document document, Orcamento orcamento) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40); // Ocupa apenas 40% da largura
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // Cálculo do subtotal (soma dos itens)
        double subtotal = 0;
        
        if (orcamento.getItensServicos() != null) {
            for (Orcamento.OrcamentoItemServico item : orcamento.getItensServicos()) {
                subtotal += item.getValorTotal();
            }
        }
        
        if (orcamento.getItensProdutos() != null) {
            for (Orcamento.OrcamentoItemProduto item : orcamento.getItensProdutos()) {
                subtotal += item.getValorTotal();
            }
        }
        
        double desconto = orcamento.getDesconto();
        double acrescimo = orcamento.getAcrescimo();
        double totalFinal = orcamento.getValorTotal();

        // Adicionar linhas de totais
        addRowTotal(table, "Subtotal:", subtotal, false);
        if (desconto > 0) {
            addRowTotal(table, "Descontos:", -desconto, false);
        }
        if (acrescimo > 0) {
            addRowTotal(table, "Acréscimos:", acrescimo, false);
        }
        
        PdfPCell cellLabel = new PdfPCell(new Phrase("TOTAL FINAL:", FONT_TOTAL_LABEL));
        cellLabel.setBorder(Rectangle.TOP);
        cellLabel.setPadding(5f);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(currencyFormat.format(totalFinal), FONT_TOTAL_VALUE));
        cellValue.setBorder(Rectangle.TOP);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValue.setPadding(5f);
        table.addCell(cellValue);

        document.add(table);
    }

    private void addRowTotal(PdfPTable table, String label, double value, boolean isBold) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, isBold ? FONT_NORMAL_BOLD : FONT_ITEM_NORMAL));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(currencyFormat.format(value), isBold ? FONT_NORMAL_BOLD : FONT_ITEM_NORMAL));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellValue);
    }

    private void adicionarObservacoes(Document document, Orcamento orcamento) throws DocumentException {
        if (orcamento.getObservacoes() != null && !orcamento.getObservacoes().trim().isEmpty()) {
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            
            PdfPCell cellHeader = new PdfPCell(new Phrase("Observações:", FONT_NORMAL_BOLD));
            cellHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cellHeader.setPadding(5f);
            table.addCell(cellHeader);

            PdfPCell cellObs = new PdfPCell(new Phrase(orcamento.getObservacoes(), FONT_ITEM_NORMAL));
            cellObs.setPadding(10f);
            table.addCell(cellObs);

            document.add(table);
        }
    }

    private void adicionarAreaAssinatura(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n\n")); // Espaço
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        table.setSpacingBefore(20f);

        PdfPCell cellEmpresa = new PdfPCell(new Paragraph("__________________________________\nAssinatura do Responsável", FONT_ITEM_NORMAL));
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        cellEmpresa.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellEmpresa);

        PdfPCell cellCliente = new PdfPCell(new Paragraph("__________________________________\nDe acordo (Cliente)", FONT_ITEM_NORMAL));
        cellCliente.setBorder(Rectangle.NO_BORDER);
        cellCliente.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellCliente);

        document.add(table);
    }

    private void adicionarRodapeSistema(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Documento gerado eletronicamente pelo app Gestão Total Mais em " + 
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), FONT_RODAPE);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}
