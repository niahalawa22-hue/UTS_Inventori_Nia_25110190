package view;

import dao.BarangDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Barang;

public class FormBarang extends JFrame {

    private final JTextField txtKode = new JTextField(15);
    private final JTextField txtNama = new JTextField(15);
    private final JComboBox<String> cmbKategori =
            new JComboBox<>(new String[]{"Komputer", "Jaringan", "Alat Ukur"});
    private final JTextField txtStok = new JTextField(15);

    private final JButton btnSimpan = new JButton("Simpan");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBersihkan = new JButton("Bersihkan");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode", "Nama Barang", "Kategori", "Stok"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(tableModel);
    private final BarangDAO dao = new BarangDAO();

    public FormBarang() {
        initUI();
        initEvent();
        tampilkanData();
    }

    private void initUI() {
        setTitle("Pengelolaan Data Barang Laboratorium");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBorder(BorderFactory.createTitledBorder("Data Barang"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        tambahBaris(panelInput, g, 0, "Kode Barang", txtKode);
        tambahBaris(panelInput, g, 1, "Nama Barang", txtNama);
        tambahBaris(panelInput, g, 2, "Kategori", cmbKategori);
        tambahBaris(panelInput, g, 3, "Stok", txtStok);
        cmbKategori.setPreferredSize(txtKode.getPreferredSize());

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelTombol.add(btnSimpan);
        panelTombol.add(btnUbah);
        panelTombol.add(btnHapus);
        panelTombol.add(btnBersihkan);

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(panelInput, BorderLayout.CENTER);
        atas.add(panelTombol, BorderLayout.SOUTH);
        atas.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(atas, BorderLayout.NORTH);

        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(tabel);
        sp.setPreferredSize(new Dimension(520, 220));
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createTitledBorder("Daftar Barang")));
        add(sp, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void tambahBaris(JPanel p, GridBagConstraints g, int baris, String label,
                             java.awt.Component komponen) {
        g.gridx = 0;
        g.gridy = baris;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(komponen, g);
    }

    private void initEvent() {
        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabel.getSelectedRow() >= 0) {
                isiFormDariTabel(tabel.getSelectedRow());
            }
        });
    }

    private void tampilkanData() {
        tableModel.setRowCount(0);
        List<Barang> list = dao.getAll();
        for (Barang b : list) {
            tableModel.addRow(new Object[]{
                b.getKodeBarang(), b.getNamaBarang(), b.getKategori(), b.getStok()});
        }
    }

    private void isiFormDariTabel(int baris) {
        txtKode.setText(tableModel.getValueAt(baris, 0).toString());
        txtNama.setText(tableModel.getValueAt(baris, 1).toString());
        cmbKategori.setSelectedItem(tableModel.getValueAt(baris, 2).toString());
        txtStok.setText(tableModel.getValueAt(baris, 3).toString());
        txtKode.setEditable(false);
    }

    private Barang validasiInput(boolean cekKodeBaru) {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String stokStr = txtStok.getText().trim();

        if (kode.isEmpty()) {
            peringatan("Kode barang tidak boleh kosong.", txtKode);
            return null;
        }
        if (nama.isEmpty()) {
            peringatan("Nama barang tidak boleh kosong.", txtNama);
            return null;
        }
        if (kode.length() > 10) {
            peringatan("Kode barang maksimal 10 karakter.", txtKode);
            return null;
        }
        if (nama.length() > 50) {
            peringatan("Nama barang maksimal 50 karakter.", txtNama);
            return null;
        }
        int stok;
        try {
            stok = Integer.parseInt(stokStr);
        } catch (NumberFormatException ex) {
            peringatan("Stok harus berupa angka bulat (misalnya 15).", txtStok);
            return null;
        }
        if (stok < 0) {
            peringatan("Stok harus berupa angka >= 0.", txtStok);
            return null;
        }
        if (cekKodeBaru && dao.kodeSudahAda(kode)) {
            peringatan("Kode barang " + kode + " sudah terdaftar.", txtKode);
            return null;
        }
        return new Barang(kode, nama, cmbKategori.getSelectedItem().toString(), stok);
    }

    private void peringatan(String pesan, JTextField fokus) {
        JOptionPane.showMessageDialog(this, pesan, "Validasi", JOptionPane.WARNING_MESSAGE);
        fokus.requestFocus();
    }

    private void simpan() {
        Barang b = validasiInput(true);
        if (b == null) return;
        if (dao.simpan(b)) {
            JOptionPane.showMessageDialog(this, "Data barang berhasil disimpan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            tampilkanData();
            bersihkan();
        } else {
            JOptionPane.showMessageDialog(this, "Data barang gagal disimpan.",
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ubah() {
        if (txtKode.getText().trim().isEmpty() || txtKode.isEditable()) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu baris data pada tabel yang akan diubah.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Barang b = validasiInput(false);
        if (b == null) return;
        int pilih = JOptionPane.showConfirmDialog(this,
                "Yakin ingin mengubah data barang " + b.getKodeBarang() + "?",
                "Konfirmasi Ubah", JOptionPane.YES_NO_OPTION);
        if (pilih != JOptionPane.YES_OPTION) return;
        if (dao.ubah(b)) {
            JOptionPane.showMessageDialog(this, "Data barang berhasil diubah.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            tampilkanData();
            bersihkan();
        } else {
            JOptionPane.showMessageDialog(this, "Data barang gagal diubah.",
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty() || txtKode.isEditable()) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu baris data pada tabel yang akan dihapus.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus data barang " + kode + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) return;
        if (dao.hapus(kode)) {
            JOptionPane.showMessageDialog(this, "Data barang berhasil dihapus.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            tampilkanData();
            bersihkan();
        } else {
            JOptionPane.showMessageDialog(this, "Data barang gagal dihapus.",
                    "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bersihkan() {
        txtKode.setText("");
        txtNama.setText("");
        txtStok.setText("");
        cmbKategori.setSelectedIndex(0);
        txtKode.setEditable(true);
        tabel.clearSelection();
        txtKode.requestFocus();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FormBarang().setVisible(true));
    }
}