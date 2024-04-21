        package com.example.examen3pm1_0076;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.util.Base64;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.media.MediaPlayer;
        import android.widget.Toast;

        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.util.List;

        public class EntrevistaAdapter extends ArrayAdapter<Entrevista> {
        private Context context;
        private List<Entrevista> entrevistas;
        private OnItemClickListener listener;
        private LayoutInflater inflater;

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;

            public interface OnItemClickListener {
                void onItemClick(Entrevista entrevista);
            }

            public EntrevistaAdapter(Context context, List<Entrevista> entrevistas, OnItemClickListener listener) {
                super(context, 0, entrevistas);
                this.context = context;
                this.entrevistas = entrevistas;
                this.listener = listener;
                this.inflater = LayoutInflater.from(context);
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                EntrevistaViewHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.entrevista_preview, parent, false);
                    holder = new EntrevistaViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (EntrevistaViewHolder) convertView.getTag();
                }
                Entrevista entrevista = getItem(position);
                holder.bind(entrevista, listener);

                return convertView;
            }

            public void eliminar(int position){
                Entrevista entrevista = getItem(position);
                if(entrevista!=null) {
                    String entrevistaID = String.valueOf(entrevista.getID());
                    DatabaseReference dataref = firebaseDatabase.getInstance().getReference("Entrevistas");
                    dataref.child(entrevistaID).removeValue().addOnSuccessListener(aVoid->{
                        Toast.makeText(getContext(),"Entrevista eliminada", Toast.LENGTH_SHORT).show();

                        remove(entrevista);
                        notifyDataSetChanged();

                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al eliminar entrevista", Toast.LENGTH_SHORT).show();
                    });

                }
            }
            class EntrevistaViewHolder {
                TextView textViewDescripcion, textViewID, textViewPeriodista;
                ImageView imageViewEntrevista;
                MediaPlayer mediaPlayer;
                View itemView;

                EntrevistaViewHolder(View view) {
                    itemView = view;
                    textViewDescripcion = view.findViewById(R.id.textViewDescripcion);
                    textViewID = view.findViewById(R.id.textViewID);
                    textViewPeriodista = view.findViewById(R.id.textViewPeriodista);
                    imageViewEntrevista = view.findViewById(R.id.imageViewEntrevista);
                }

                void bind(final Entrevista entrevista, final OnItemClickListener listener) {
                    textViewDescripcion.setText(entrevista.getDescricpion());
                    //textViewID.setText(String.valueOf(entrevista.getID()));
                    textViewPeriodista.setText(entrevista.getPeriodista());
                    //setImage(entrevista.getImagenBase64());

                    /*itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {listener.onItemClick(entrevista);
                            playAudio(entrevista.getAudioBase64());
                        }
                    });*/
                }

                private void setImage(String base64Image) {
                    if (base64Image != null && !base64Image.isEmpty()) {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageViewEntrevista.setImageBitmap(decodedByte);
                    }
                }

                private void playAudio(String base64Audio) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }

                    mediaPlayer = new MediaPlayer();
                    try {
                        File tempAudio = convertBase64ToTempFile(base64Audio);
                        mediaPlayer.setDataSource(tempAudio.getAbsolutePath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(mp -> tempAudio.delete());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                private File convertBase64ToTempFile(String base64Audio) throws IOException {
                    byte[] decodedBytes = Base64.decode(base64Audio, Base64.DEFAULT);
                    File tempAudioFile = File.createTempFile("tempAudio", "3gp", itemView.getContext().getCacheDir());
                    try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                        fos.write(decodedBytes);
                    }
                    return tempAudioFile;
                }


            }
        }