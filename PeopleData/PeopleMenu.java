import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
    // deleteButton hanya tampil saat mode edit (di-bind lewat GUI Designer)
    private JButton deleteButton;
    private JTable tabelPlayer;

    private ArrayList<Person> personList;

    // konstruktor
    public PeopleMenu(){
        personList = new ArrayList<>();
        // menambahkan data hardcode
        personList.add(new Person("PL001","f0rsakeN","Indonesia",2004,"Duelist", "Masters Champion"));
        personList.add(new Person("PL002","mindfreak","Indonesia",1999,"Controller", "Masters Champion"));
        personList.add(new Person("PL003","Jinggg","Singapura",2003,"Duelist", "Masters Champion"));
        personList.add(new Person("PL004","TenZ","Kanada",2001,"Duelist", "Masters Champion"));
        personList.add(new Person("PL005","aspas","Brasil",2003,"Duelist","World Champion"));

        setComboBox();
        setRadioButton();
        setTable();
        setTableSelection();   // klik baris tabel -> data tampil di form
        setButtonAction();
        setButtonState(false); // awalnya mode tambah: tombol Add + Cancel
    }

    // ===================== VALIDASI & PEMBACAAN FORM =====================

    // membaca dan memvalidasi isi form, dipakai oleh Add dan Update
    // mengembalikan objek Person, atau null jika ada input yang tidak valid
    private Person readForm(){
        String id = idField.getText().trim().toUpperCase();
        String nama = namaField.getText().trim();
        String negara = negaraField.getText().trim();
        String tahunText = tahunField.getText().trim();

        // semua field teks wajib diisi
        if (id.isEmpty() || nama.isEmpty() || negara.isEmpty() || tahunText.isEmpty()){
            JOptionPane.showMessageDialog(null, "Semua field wajib diisi!");
            return null;
        }

        // tahun lahir harus berupa angka
        int tahunLahir;
        try {
            tahunLahir = Integer.parseInt(tahunText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Tahun lahir harus berupa angka!");
            tahunField.requestFocus();
            return null;
        }

        // tahun lahir harus masuk akal (1900 sampai tahun sekarang)
        int tahunSekarang = Calendar.getInstance().get(Calendar.YEAR);
        if (tahunLahir < 1900 || tahunLahir > tahunSekarang){
            JOptionPane.showMessageDialog(null, "Tahun lahir tidak valid!");
            tahunField.requestFocus();
            return null;
        }

        // index 0 adalah "-- Pilih Role --" (belum memilih)
        if (roleBox.getSelectedIndex() == 0){
            JOptionPane.showMessageDialog(null, "Pilih role terlebih dahulu!");
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
            JOptionPane.showMessageDialog(null, "Pilih skill level terlebih dahulu!");
            return null;
        }

        return new Person(id, nama, negara, tahunLahir, role, skillLevel);
    }

    // ===================== ADD / UPDATE / DELETE =====================

    // menambah data baru
    public void insertData(){
        Person baru = readForm();
        if (baru == null){
            return; // input tidak valid, pesan sudah ditampilkan
        }

        // cegah ID ganda
        for (Person p : personList){
            if (p.getId().equalsIgnoreCase(baru.getId())){
                JOptionPane.showMessageDialog(null, "ID sudah digunakan!");
                return;
            }
        }

        // menambahkan objek ke list lalu muat ulang tabel
        personList.add(baru);
        setTable();
        clearForm();
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

        // urutan baris tabel sama dengan urutan personList
        // ID tidak diubah (field ID dikunci saat edit), sisanya diperbarui lewat setter
        Person lama = personList.get(row);
        lama.setNama(baru.getNama());
        lama.setNegara(baru.getNegara());
        lama.setTahunLahir(baru.getTahunLahir());
        lama.setRole(baru.getRole());
        lama.setSkillLevel(baru.getSkillLevel());

        setTable();
        clearForm();
        JOptionPane.showMessageDialog(null, "Data berhasil diperbarui!");
    }

    // menghapus data terpilih setelah konfirmasi
    public void deleteData(){
        int row = tabelPlayer.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(null, "Pilih data yang akan dihapus terlebih dahulu!");
            return;
        }

        Person p = personList.get(row);

        // CONFIRMATION PROMPT: tanya dulu sebelum menghapus
        int pilihan = JOptionPane.showConfirmDialog(
                null,
                "Yakin ingin menghapus data " + p.getNama() + " (" + p.getId() + ")?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // hapus hanya jika pengguna memilih "Yes"
        if (pilihan == JOptionPane.YES_OPTION){
            personList.remove(row);
            setTable();
            clearForm();
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

    // saat baris tabel diklik, datanya ditampilkan di form
    public void setTableSelection(){
        // hanya boleh memilih satu baris
        tabelPlayer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabelPlayer.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // abaikan event saat pilihan masih berubah
                if (e.getValueIsAdjusting()){
                    return;
                }
                int row = tabelPlayer.getSelectedRow();
                // row == -1 artinya tidak ada baris terpilih (mis. setelah clearSelection)
                if (row != -1){
                    fillForm(personList.get(row));
                    setButtonState(true); // mode edit
                }
            }
        });
    }

    public void setTable(){
        String[] columnNames = {"ID", "Nama", "Negara", "Tahun Lahir","Kategori", "Skill Level"};
        // sel tabel tidak bisa diedit langsung, ubah data lewat form + tombol Update
        DefaultTableModel model = new DefaultTableModel(columnNames, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // menambahkan data dari ArrayList ke tabel
        for(Person person : personList){
            Object[] rowData = {person.getId(), person.getNama(), person.getNegara(), person.getTahunLahir(), person.getRole(), person.getSkillLevel()};
            model.addRow(rowData);
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