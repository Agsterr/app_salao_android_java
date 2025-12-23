package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classe utilitária para gerar PDFs de relatórios.
 */
public class PDFGeneratorHelper {

    private static final String TAG = "PDFGeneratorHelper";
    
    // Fontes
    private static final Font FONT_TITULO = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10);
    private static final Font FONT_NORMAL_BOLD = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_PEQUENO = new Font(Font.FontFamily.HELVETICA, 8);

    /**
     * Gera um PDF de relatório de produtos.
     */
    public static File gerarPDFProdutos(Context context, List<Produto> produtos) throws IOException, DocumentException {
        File documentsDir = new File(context.getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String nomeArquivo = "Relatorio_Produtos_" + sdfNome.format(new Date()) + ".pdf";
        File pdfFile = new File(documentsDir, nomeArquivo);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Título
        document.add(new Paragraph("RELATÓRIO DE PRODUTOS", FONT_TITULO));
        document.add(new Paragraph(" "));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Tabela
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});

        // Cabeçalho
        table.addCell(new PdfPCell(new Paragraph("Nome", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Preço Aquisição", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Preço Venda", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Lucro Unitário", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Margem %", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Descrição", FONT_NORMAL_BOLD)));

        double totalLucroPotencial = 0.0;
        
        // Dados
        for (Produto produto : produtos) {
            table.addCell(new PdfPCell(new Paragraph(produto.getNome() != null ? produto.getNome() : "", FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(nf.format(produto.getPrecoAquisicao()), FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(nf.format(produto.getPrecoVenda()), FONT_NORMAL)));
            
            double lucroUnitario = produto.getLucroUnitario();
            table.addCell(new PdfPCell(new Paragraph(nf.format(lucroUnitario), FONT_NORMAL)));
            
            double margem = produto.getMargemLucro();
            table.addCell(new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "%.2f%%", margem), FONT_NORMAL)));
            
            table.addCell(new PdfPCell(new Paragraph(produto.getDescricao() != null ? produto.getDescricao() : "", FONT_NORMAL)));
            
            totalLucroPotencial += lucroUnitario;
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de produtos: " + produtos.size(), FONT_NORMAL_BOLD));
        document.add(new Paragraph("Lucro Potencial Total: " + nf.format(totalLucroPotencial), FONT_SUBTITULO));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Gerado em: " + sdf.format(new Date()), FONT_PEQUENO));

        document.close();
        return pdfFile;
    }

    /**
     * Gera uma ficha simples de produtos com nome e valor para enviar ao cliente.
     */
    public static File gerarFichaProdutos(Context context, List<Produto> produtos) throws IOException, DocumentException {
        File documentsDir = new File(context.getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String nomeArquivo = "Ficha_Produtos_" + sdfNome.format(new Date()) + ".pdf";
        File pdfFile = new File(documentsDir, nomeArquivo);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Título
        document.add(new Paragraph("FICHA DE PRODUTOS", FONT_TITULO));
        document.add(new Paragraph(" "));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Tabela simples com apenas nome e preço de venda
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f});

        // Cabeçalho
        table.addCell(new PdfPCell(new Paragraph("Produto", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Preço", FONT_NORMAL_BOLD)));

        // Dados
        for (Produto produto : produtos) {
            table.addCell(new PdfPCell(new Paragraph(produto.getNome() != null ? produto.getNome() : "", FONT_NORMAL)));
            double precoVenda = produto.getPrecoVenda() > 0 ? produto.getPrecoVenda() : produto.getValorPadrao();
            table.addCell(new PdfPCell(new Paragraph(nf.format(precoVenda), FONT_NORMAL)));
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de produtos: " + produtos.size(), FONT_NORMAL_BOLD));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Gerado em: " + sdf.format(new Date()), FONT_PEQUENO));

        document.close();
        return pdfFile;
    }

    /**
     * Gera um PDF de relatório de vendas.
     */
    public static File gerarPDFVendas(Context context, List<Venda> vendas, VendaDAO vendaDAO, 
                                      ProdutoDAO produtoDAO, ClienteDAO clienteDAO, 
                                      VendaItemDAO vendaItemDAO) throws IOException, DocumentException {
        File documentsDir = new File(context.getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String nomeArquivo = "Relatorio_Vendas_" + sdfNome.format(new Date()) + ".pdf";
        File pdfFile = new File(documentsDir, nomeArquivo);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Título
        document.add(new Paragraph("RELATÓRIO DE VENDAS DE PRODUTOS", FONT_TITULO));
        document.add(new Paragraph(" "));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

        double totalVendas = 0;
        double totalLucro = 0;
        int totalAvista = 0;
        int totalAprazo = 0;

        // Tabela
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.8f, 2f, 1.5f, 1.5f, 1.5f, 1.2f});

        // Cabeçalho
        table.addCell(new PdfPCell(new Paragraph("Data/Hora", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Cliente", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Produtos", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Tipo Pag.", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Valor Total", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Lucro", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Obs.", FONT_NORMAL_BOLD)));

        // Dados
        for (Venda venda : vendas) {
            String dataHora = sdfData.format(new Date(venda.getDataVenda())) + " " + 
                             sdfHora.format(new Date(venda.getDataVenda()));
            
            Cliente cliente = clienteDAO.getClienteById(venda.getClienteId());
            String clienteNome = cliente != null ? cliente.getNome() : "Cliente";
            
            // Obter produtos da venda e calcular lucro
            List<VendaItem> itens = vendaItemDAO.getItensByVendaId(venda.getId());
            StringBuilder produtosStr = new StringBuilder();
            double lucroVenda = 0.0;
            
            if (!itens.isEmpty()) {
                for (int i = 0; i < itens.size(); i++) {
                    VendaItem item = itens.get(i);
                    Produto p = produtoDAO.getProdutoById(item.getProdutoId());
                    if (p != null) {
                        if (i > 0) produtosStr.append(", ");
                        produtosStr.append(p.getNome()).append(" x").append(item.getQuantidade());
                        // Calcular lucro: (preço de venda - preço de aquisição) * quantidade
                        double lucroUnitario = p.getPrecoVenda() - p.getPrecoAquisicao();
                        lucroVenda += lucroUnitario * item.getQuantidade();
                    }
                }
            } else {
                Produto p = produtoDAO.getProdutoById(venda.getProdutoId());
                if (p != null) {
                    produtosStr.append(p.getNome());
                    // Calcular lucro para venda antiga (sem itens)
                    double lucroUnitario = p.getPrecoVenda() - p.getPrecoAquisicao();
                    lucroVenda += lucroUnitario; // Assumir quantidade 1
                } else {
                    produtosStr.append("Produto");
                }
            }
            
            String tipoPagamento = venda.getTipoPagamento() == VendaDAO.TIPO_AVISTA ? "À vista" : "A prazo";
            if (venda.getTipoPagamento() == VendaDAO.TIPO_AVISTA) {
                totalAvista++;
            } else {
                totalAprazo++;
            }
            
            String observacao = venda.getObservacao() != null ? venda.getObservacao() : "";
            
            table.addCell(new PdfPCell(new Paragraph(dataHora, FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(clienteNome, FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(produtosStr.toString(), FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(tipoPagamento, FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(nf.format(venda.getValorTotal()), FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(nf.format(lucroVenda), FONT_NORMAL)));
            table.addCell(new PdfPCell(new Paragraph(observacao.length() > 10 ? observacao.substring(0, 10) + "..." : observacao, FONT_NORMAL)));
            
            totalVendas += venda.getValorTotal();
            totalLucro += lucroVenda;
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de vendas: " + vendas.size(), FONT_NORMAL_BOLD));
        document.add(new Paragraph("Vendas à vista: " + totalAvista, FONT_NORMAL));
        document.add(new Paragraph("Vendas a prazo: " + totalAprazo, FONT_NORMAL));
        document.add(new Paragraph("Faturamento Total: " + nf.format(totalVendas), FONT_SUBTITULO));
        document.add(new Paragraph("💰 Lucro Total: " + nf.format(totalLucro), FONT_SUBTITULO));
        double margemLucroTotal = totalVendas > 0 ? (totalLucro / totalVendas) * 100 : 0;
        document.add(new Paragraph("Margem de Lucro: " + String.format(Locale.getDefault(), "%.2f%%", margemLucroTotal), FONT_NORMAL_BOLD));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Gerado em: " + sdfData.format(new Date()) + " " + sdfHora.format(new Date()), FONT_PEQUENO));

        document.close();
        return pdfFile;
    }

    /**
     * Gera um PDF de relatório de recebimentos (valores a receber ou valores recebidos).
     */
    public static File gerarPDFRecebimentos(Context context, List<Recebimento> recebimentos, 
                                            int status, VendaDAO vendaDAO, ClienteDAO clienteDAO) 
                                            throws IOException, DocumentException {
        File documentsDir = new File(context.getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String tipoRelatorio = (status == RecebimentoDAO.STATUS_A_RECEBER) ? "AReceber" : "Recebidos";
        String nomeArquivo = "Relatorio_" + tipoRelatorio + "_" + sdfNome.format(new Date()) + ".pdf";
        File pdfFile = new File(documentsDir, nomeArquivo);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Título
        String titulo = (status == RecebimentoDAO.STATUS_A_RECEBER) ? 
            "RELATÓRIO DE VALORES A RECEBER" : "RELATÓRIO DE VALORES RECEBIDOS";
        document.add(new Paragraph(titulo, FONT_TITULO));
        document.add(new Paragraph(" "));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

        double totalValor = 0;

        // Agrupar por cliente
        java.util.Map<Long, java.util.List<Recebimento>> porCliente = new java.util.HashMap<>();
        for (Recebimento r : recebimentos) {
            Venda venda = vendaDAO.getVendaById(r.getVendaId());
            Long cid = (venda != null) ? venda.getClienteId() : -1L;
            java.util.List<Recebimento> lr = porCliente.get(cid);
            if (lr == null) {
                lr = new java.util.ArrayList<>();
                porCliente.put(cid, lr);
            }
            lr.add(r);
        }

        // Tabela
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1.5f, 2f, 2f, 1.5f});

        // Cabeçalho
        table.addCell(new PdfPCell(new Paragraph("Cliente", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Parcela", FONT_NORMAL_BOLD)));
        table.addCell(new PdfPCell(new Paragraph("Data Prevista", FONT_NORMAL_BOLD)));
        if (status == RecebimentoDAO.STATUS_PAGO) {
            table.addCell(new PdfPCell(new Paragraph("Data Pagamento", FONT_NORMAL_BOLD)));
        } else {
            table.addCell(new PdfPCell(new Paragraph("Status", FONT_NORMAL_BOLD)));
        }
        table.addCell(new PdfPCell(new Paragraph("Valor", FONT_NORMAL_BOLD)));

        // Dados
        for (java.util.Map.Entry<Long, java.util.List<Recebimento>> entry : porCliente.entrySet()) {
            Long cid = entry.getKey();
            Cliente cliente = (cid != -1L) ? clienteDAO.getClienteById(cid) : null;
            String clienteNome = cliente != null ? cliente.getNome() : "Cliente desconhecido";
            
            for (Recebimento r : entry.getValue()) {
                table.addCell(new PdfPCell(new Paragraph(clienteNome, FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph("Parcela " + r.getNumeroParcela(), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(sdf.format(new Date(r.getDataPrevista())), FONT_NORMAL)));
                
                if (status == RecebimentoDAO.STATUS_PAGO) {
                    String dataPagamento = r.getDataPagamento() > 0 ? 
                        sdf.format(new Date(r.getDataPagamento())) : "Não pago";
                    table.addCell(new PdfPCell(new Paragraph(dataPagamento, FONT_NORMAL)));
                } else {
                    table.addCell(new PdfPCell(new Paragraph("Pendente", FONT_NORMAL)));
                }
                
                table.addCell(new PdfPCell(new Paragraph(nf.format(r.getValor()), FONT_NORMAL)));
                totalValor += r.getValor();
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de parcelas: " + recebimentos.size(), FONT_NORMAL_BOLD));
        document.add(new Paragraph("Valor Total: " + nf.format(totalValor), FONT_SUBTITULO));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Gerado em: " + sdf.format(new Date()) + " " + sdfHora.format(new Date()), FONT_PEQUENO));

        document.close();
        return pdfFile;
    }

    /**
     * Gera um PDF de orçamento.
     */
    public static File gerarPDFOrcamento(Context context, Orcamento orcamento, Cliente cliente) 
            throws IOException, DocumentException {
        File documentsDir = new File(context.getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String nomeArquivo = "Orcamento_" + sdfNome.format(new Date()) + ".pdf";
        File pdfFile = new File(documentsDir, nomeArquivo);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Título
        document.add(new Paragraph("ORÇAMENTO", FONT_TITULO));
        document.add(new Paragraph(" "));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Informações do cliente
        document.add(new Paragraph("Cliente: " + cliente.getNome(), FONT_NORMAL_BOLD));
        document.add(new Paragraph("Data: " + sdfData.format(new Date(orcamento.getDataCriacao())) + 
            " às " + sdfHora.format(new Date(orcamento.getDataCriacao())), FONT_NORMAL));
        document.add(new Paragraph("Tipo: " + ("SERVICO".equals(orcamento.getTipo()) ? "Serviços" : "Produtos"), FONT_NORMAL));
        document.add(new Paragraph(" "));

        // Tabela de itens
        PdfPTable table;
        if ("SERVICO".equals(orcamento.getTipo())) {
            table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1.5f, 2f, 2f});

            table.addCell(new PdfPCell(new Paragraph("Serviço", FONT_NORMAL_BOLD)));
            table.addCell(new PdfPCell(new Paragraph("Quantidade", FONT_NORMAL_BOLD)));
            table.addCell(new PdfPCell(new Paragraph("Valor Unitário", FONT_NORMAL_BOLD)));
            table.addCell(new PdfPCell(new Paragraph("Valor Total", FONT_NORMAL_BOLD)));

            for (Orcamento.OrcamentoItemServico item : orcamento.getItensServicos()) {
                table.addCell(new PdfPCell(new Paragraph(item.getNomeServico(), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(item.getQuantidade()), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(nf.format(item.getValorUnitario()), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(nf.format(item.getValorTotal()), FONT_NORMAL)));
            }
        } else {
            table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1.5f, 2f, 2f});

            table.addCell(new PdfPCell(new Paragraph("Produto", FONT_NORMAL_BOLD)));
            table.addCell(new PdfPCell(new Paragraph("Quantidade", FONT_NORMAL_BOLD)));
            table.addCell(new PdfPCell(new Paragraph("Valor Unitário", FONT_NORMAL_BOLD)));
            table.addCell(new PdfPCell(new Paragraph("Valor Total", FONT_NORMAL_BOLD)));

            for (Orcamento.OrcamentoItemProduto item : orcamento.getItensProdutos()) {
                table.addCell(new PdfPCell(new Paragraph(item.getNomeProduto(), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(item.getQuantidade()), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(nf.format(item.getValorUnitario()), FONT_NORMAL)));
                table.addCell(new PdfPCell(new Paragraph(nf.format(item.getValorTotal()), FONT_NORMAL)));
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("TOTAL: " + nf.format(orcamento.getValorTotal()), FONT_SUBTITULO));
        document.add(new Paragraph(" "));

        if (orcamento.getObservacoes() != null && !orcamento.getObservacoes().trim().isEmpty()) {
            document.add(new Paragraph("Observações:", FONT_NORMAL_BOLD));
            document.add(new Paragraph(orcamento.getObservacoes(), FONT_NORMAL));
            document.add(new Paragraph(" "));
        }

        document.add(new Paragraph("Gerado em: " + sdfData.format(new Date()) + " " + sdfHora.format(new Date()), FONT_PEQUENO));

        document.close();
        return pdfFile;
    }

    /**
     * Compartilha um arquivo PDF usando Intent.
     */
    public static void compartilharPDF(Context context, File pdfFile) {
        try {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                pdfFile
            );
            
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Relatório - " + pdfFile.getName());
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Relatório gerado pelo app Gerenciamento Total Mais");
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }
            
            String tituloChooser = "Exportar Relatório";
            android.content.Intent chooser = android.content.Intent.createChooser(shareIntent, tituloChooser);
            
            if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao compartilhar PDF", e);
        }
    }
}

