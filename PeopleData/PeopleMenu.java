import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;

public class PeopleMenu {
    private JPanel mainPanel;
    private JLabel dataPemainEsportLabel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tahunField;
    private JTextField negaraField;
    private JComboBox<String> roleBox;
    private JRadioButton regionalChampionRadioButton;
    private JRadioButton mastersRadioButton;
    private JRadioButton wcRadioButton;
    private ButtonGroup skillLevelGroup;

    // addButton berfungsi ganda: "Add" (mode tambah) dan "Update" (mode edit)
    private JButton addButton;
    private JButton cancelButton;
    // deleteButton hanya tampil saat mode edit
    private JButton deleteButton;
    private JTable tabelPlayer;

    // ArrayList dihapus: data dibaca/ditulis langsung ke MySQL lewat kelas Database
    private Database database;

    // konstruktor
    public PeopleMenu(){
        // konstruktor Database membuka koneksi ke MySQL, melempar RuntimeException jika gagal
        try {
            database = new Database();
        } catch (RuntimeException ex) {
            showDbError(ex);
            System.exit(1); // tanpa koneksi, program tidak bisa dipakai
        }

        setComboBox();
        setRadioButton();
        setTable();            // data dimuat dari database
        setTableSelection();   // klik baris tabel -> data tampil di form
        setButtonAction();
        setButtonState(false); // awalnya mode tambah: tombol Add + Cancel
    }

    // menampilkan pesan error database dalam dialog
    private void showDbError(Exception ex){
        JOptionPane.showMessageDialog(null,
                "Terjadi kesalahan database:\n" + ex.getMessage(),
                "Error Database",
                JOptionPane.ERROR_MESSAGE);
    }

    // memberi escape pada tanda ' dan \ supaya teks seperti O'Brien tidak merusak query SQL
    private String esc(String teks){
        return teks.replace("\\", "\\\\").replace("'", "''");
    }

    // ===================== VALIDASI & PEMBACAAN FORM =====================

    // membaca dan memvalidasi isi form, dipakai oleh Add dan Update
    // mengembalikan objek Person, atau null jika ada input yang tidak valid
    private Person readForm(){
        String id = idField.getText().trim().toUpperCase();
        String nama = namaField.getText().trim();
        String negara = negaraField.getText().trim();
        String tahunText = tahunField.getText().trim();

        // semua field teks wajib diisi, jika ada yang kosong tampilkan dialog error
        if (id.isEmpty() || nama.isEmpty() || negara.isEmpty() || tahunText.isEmpty()){
            JOptionPane.showMessageDialog(null, "Semua field wajib diisi!",
                    "Input Kosong", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // tahun lahir harus berupa angka
        int tahunLahir;
        try {
            tahunLahir = Integer.parseInt(tahunText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Tahun lahir harus berupa angka!",
                    "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            tahunField.requestFocus();
            return null;
        }

        // tahun lahir harus masuk akal (1900 sampai tahun sekarang)
        int tahunSekarang = Calendar.getInstance().get(Calendar.YEAR);
        if (tahunLahir < 1900 || tahunLahir > tahunSekarang){
            JOptionPane.showMessageDialog(null, "Tahun lahir tidak valid!",
                    "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            tahunField.requestFocus();
            return null;
        }

        // index 0 adalah "-- Pilih Role --" (belum memilih), dianggap kolom kosong
        if (roleBox.getSelectedIndex() == 0){
            JOptionPane.showMessageDialog(null, "Pilih role terlebih dahulu!",
                    "Input Kosong", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String role = roleBox.getSelectedItem().toString();

        // mengambil pilihan dari radiobutton
        // menelusuri semua radio button di grup, mencari yang sedang dipilih
        String skillLevel = "";
        for (Enumeration<AbstractButton> buttons = skillLevelGroup.getElements() ; buttons.hasMoreElements();){
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()){
                skillLevel = button.getText();
            }
        }

        // memastikan ada radio button yang dipilih
        if (skillLevel.isEmpty()){
            JOptionPane.showMessageDialog(null, "Pilih skill level terlebih dahulu!",
                    "Input Kosong", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return new Person(id, nama, negara, tahunLahir, role, skillLevel);
    }

    // ===================== ADD / UPDATE / DELETE (terhubung ke MySQL) =====================

    // menambah data baru ke database
    public void insertData(){
        Person baru = readForm();
        if (baru == null){
            return; // input tidak valid, pesan sudah ditampilkan
        }

        try {
            // cegah ID ganda: cek langsung ke database lewat SELECT
            ResultSet rs = database.selectQuery("SELECT id FROM person WHERE id = '" + esc(baru.getId()) + "'");
            if (rs.next()){ // ada baris -> ID sudah dipakai
                JOptionPane.showMessageDialog(null,
                        "ID " + baru.getId() + " sudah digunakan! Gunakan ID lain.",
                        "ID Ganda", JOptionPane.ERROR_MESSAGE);
                idField.requestFocus();
                return;
            }

            // simpan ke database lewat INSERT
            String sql = "INSERT INTO person (id, nama, negara, tahun_lahir, role, skill_level) VALUES ('"
                    + esc(baru.getId()) + "', '"
                    + esc(baru.getNama()) + "', '"
                    + esc(baru.getNegara()) + "', "
                    + baru.getTahunLahir() + ", '"
                    + esc(baru.getRole()) + "', '"
                    + esc(baru.getSkillLevel()) + "')";
            database.insertUpdateDeleteQuery(sql);

            // muat ulang tabel dari database
            setTable();
            clearForm();
        } catch (SQLException | RuntimeException ex) {
            showDbError(ex);
        }
    }

    // mengubah data pada baris yang sedang dipilih
    public void updateData(){
        // nomor baris yang dipilih (-1 jika tidak ada)
        int row = tabelPlayer.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(null, "Pilih data yang akan diubah terlebih dahulu!");
            return;
        }

        Person baru = readForm();
        if (baru == null){
            return;
        }

        try {
            // ID dikunci saat edit, jadi WHERE id = ... selalu menunjuk baris yang benar
            String sql = "UPDATE person SET "
                    + "nama = '" + esc(baru.getNama()) + "', "
                    + "negara = '" + esc(baru.getNegara()) + "', "
                    + "tahun_lahir = " + baru.getTahunLahir() + ", "
                    + "role = '" + esc(baru.getRole()) + "', "
                    + "skill_level = '" + esc(baru.getSkillLevel()) + "' "
                    + "WHERE id = '" + esc(baru.getId()) + "'";
            database.insertUpdateDeleteQuery(sql);

            setTable();
            clearForm();
            JOptionPane.showMessageDialog(null, "Data berhasil diperbarui!");
        } catch (RuntimeException ex) {
            showDbError(ex);
        }
    }

    // menghapus data terpilih setelah konfirmasi
    public void deleteData(){
        int row = tabelPlayer.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(null, "Pilih data yang akan dihapus terlebih dahulu!");
            return;
        }

        // ambil ID dan nama langsung dari tabel
        String id = tabelPlayer.getValueAt(row, 0).toString();
        String nama = tabelPlayer.getValueAt(row, 1).toString();

        // CONFIRMATION PROMPT: tanya dulu sebelum menghapus
        int pilihan = JOptionPane.showConfirmDialog(
                null,
                "Yakin ingin menghapus data " + nama + " (" + id + ")?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // hapus hanya jika pengguna memilih "Yes"
        if (pilihan == JOptionPane.YES_OPTION){
            try {
                database.insertUpdateDeleteQuery("DELETE FROM person WHERE id = '" + esc(id) + "'");
                setTable();
                clearForm();
            } catch (RuntimeException ex) {
                showDbError(ex);
            }
        }
    }

    // ===================== SETTER / METHOD PENGATUR =====================

    public void setComboBox(){
        roleBox.removeAllItems();

        String[] daftarRole = {"-- Pilih Role --", "Duelist", "Controller", "Initiator", "Sentinel"};
        for (String role : daftarRole){
            roleBox.addItem(role);
        }

        roleBox.setSelectedIndex(0);
    }

    public void setRadioButton(){
        skillLevelGroup = new ButtonGroup();
        skillLevelGroup.add(mastersRadioButton);
        skillLevelGroup.add(regionalChampionRadioButton);
        skillLevelGroup.add(wcRadioButton);
    }

    public void setButtonAction(){
        // tombol ini berfungsi sebagai Add (mode tambah) atau Update (mode edit)
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // tidak ada baris terpilih -> tambah data baru, ada -> ubah data
                if (tabelPlayer.getSelectedRow() == -1){
                    insertData();
                } else {
                    updateData();
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });
    }

    // saat baris tabel diklik, nilainya diambil dari tabel (bukan dari ArrayList) lalu ditampilkan di form
    public void setTableSelection(){
        // hanya boleh memilih satu baris
        tabelPlayer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabelPlayer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // index baris yang diklik pada tabel
                int row = tabelPlayer.getSelectedRow();
                if (row == -1){
                    return;
                }

                // mengambil nilai tiap kolom: getValueAt(baris, kolom)
                String id = tabelPlayer.getValueAt(row, 0).toString();
                String nama = tabelPlayer.getValueAt(row, 1).toString();
                String negara = tabelPlayer.getValueAt(row, 2).toString();
                int tahunLahir = Integer.parseInt(tabelPlayer.getValueAt(row, 3).toString());
                String role = tabelPlayer.getValueAt(row, 4).toString();
                String skillLevel = tabelPlayer.getValueAt(row, 5).toString();

                fillForm(new Person(id, nama, negara, tahunLahir, role, skillLevel));
                setButtonState(true); // mode edit
            }
        });
    }

    // memuat ulang isi tabel dari database
    public void setTable(){
        String[] columnNames = {"ID", "Nama", "Negara", "Tahun Lahir","Kategori", "Skill Level"};
        // sel tabel tidak bisa diedit langsung, ubah data lewat form + tombol Update
        DefaultTableModel model = new DefaultTableModel(columnNames, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            // selectQuery() mengembalikan ResultSet hasil query SELECT
            ResultSet rs = database.selectQuery("SELECT id, nama, negara, tahun_lahir, role, skill_level FROM person ORDER BY id");
            // rs.next() maju ke baris berikutnya, false jika sudah habis
            while (rs.next()){
                // langsung memasukkan tiap baris hasil query ke model tabel
                Object[] rowData = {
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getString("negara"),
                        rs.getInt("tahun_lahir"),
                        rs.getString("role"),
                        rs.getString("skill_level")};
                model.addRow(rowData);
            }
        } catch (SQLException | RuntimeException ex) {
            showDbError(ex);
        }
        tabelPlayer.setModel(model);
    }

    // menampilkan data Person ke dalam form
    public void fillForm(Person p){
        idField.setText(p.getId());
        idField.setEditable(false); // ID tidak boleh diubah saat edit
        namaField.setText(p.getNama());
        negaraField.setText(p.getNegara());
        tahunField.setText(String.valueOf(p.getTahunLahir()));
        roleBox.setSelectedItem(p.getRole());

        // pilih radio button yang teksnya sama dengan skill level data
        for (Enumeration<AbstractButton> buttons = skillLevelGroup.getElements() ; buttons.hasMoreElements();){
            AbstractButton button = buttons.nextElement();
            if (button.getText().equals(p.getSkillLevel())){
                button.setSelected(true);
            }
        }
    }

    // true  = baris dipilih: tombol "Update" + Delete + Cancel
    // false = mode tambah  : tombol "Add" + Cancel
    public void setButtonState(boolean sedangEdit){
        // teks tombol berubah sesuai mode
        addButton.setText(sedangEdit ? "Update" : "Add");

        // Delete hanya muncul saat mode edit
        deleteButton.setVisible(sedangEdit);
    }

    public void clearForm(){
        idField.setText("");
        idField.setEditable(true);    // ID bisa diisi lagi untuk data baru
        namaField.setText("");
        negaraField.setText("");
        tahunField.setText("");
        roleBox.setSelectedIndex(0);
        skillLevelGroup.clearSelection();
        tabelPlayer.clearSelection(); // hilangkan sorotan baris
        setButtonState(false);        // kembali ke mode tambah
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Player Table");
        PeopleMenu menu = new PeopleMenu();

        frame.setContentPane(menu.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // jangan pakai pack(), karena akan menimpa setSize()
        // ukuran dibuat lebih lebar supaya semua komponen muat
        frame.setSize(800, 600);

        // ukuran minimal, supaya window tidak bisa diperkecil sampai terpotong lagi
        frame.setMinimumSize(new java.awt.Dimension(700, 500));

        // letakkan di tengah layar (setelah ukuran ditentukan)
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}