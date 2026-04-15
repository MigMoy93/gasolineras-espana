private static Map<String, List<RegistroHistorico>> cargarHistorico() {

    Map<String, List<RegistroHistorico>> historico = new HashMap<>();

    File file = new File(RUTA);

    // Si no existe el archivo, devolver mapa vacio
    if (!file.exists()) {
        return historico;
    }

    try (FileReader reader = new FileReader(file)) {

        JsonElement elemento = JsonParser.parseReader(reader);

        // Si no es un array, se ignora
        if (!elemento.isJsonArray()) {
            return historico;
        }

        JsonArray array = elemento.getAsJsonArray();

        // Recorrer cada registro
        for (JsonElement e : array) {

            JsonObject obj = e.getAsJsonObject();

            String id = obj.get("id_gasolinera").getAsString();
            String fecha = obj.get("fecha").getAsString();

            // Leer valores, permitiendo null y claves vacias
            JsonElement gasolinaElement = obj.get("precio_gasolina");
            JsonElement dieselElement = obj.get("precio_diesel");

            Double p95 = (gasolinaElement == null || gasolinaElement.isJsonNull())
                    ? null
                    : gasolinaElement.getAsDouble();

            Double diesel = (dieselElement == null || dieselElement.isJsonNull())
                    ? null
                    : dieselElement.getAsDouble();

            List<RegistroHistorico> lista = historico.get(id);

            if (lista == null) {
                lista = new ArrayList<>();
                historico.put(id, lista);
            }

            lista.add(new RegistroHistorico(fecha, p95, diesel));
        }

    } catch (Exception e) {
        throw new RuntimeException("Error leyendo historico", e);
    }

    return historico;
}
