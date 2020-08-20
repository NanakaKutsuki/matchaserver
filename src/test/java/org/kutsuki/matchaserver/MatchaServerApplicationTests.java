package org.kutsuki.matchaserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.matchaserver.document.Hotel;
import org.kutsuki.matchaserver.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MatchaServerApplicationTests {
    @Autowired
    private HotelRepository repository;

    @Test
    public void contextLoads() {
//	try (BufferedReader br = new BufferedReader(
//		new FileReader(new File("C:\\Users\\MatchaGreen\\Desktop\\locations.json")))) {
//	    int id = 1;
//	    String line = "";
//
//	    while ((line = br.readLine()) != null) {
//		ObjectMapper mapper = new ObjectMapper();
//		Model model = mapper.readValue(line, Model.class);
//
//		City city = new City();
//		city.setId(model.getId());
//		city.setCity(model.getLocation());
//		city.setEmail(model.getEmail());
//		repository.insert(city);
//		id++;
//	    }
//	} catch (IOException e) {
//	    e.printStackTrace();
//	}

	int count = 0;
	for (Hotel hotel : repository.findAll()) {
	    if (hotel.getCityId().equals("1") && hotel.isActive()) {
		count++;
	    }
	}

	System.out.println(count);

	Assertions.assertTrue(count == repository.findAllByCityIdAndActive("1", true).size(),
		"Not same number of active");

	Assertions.assertTrue(count > 0, "Count is 0");
    }

}
