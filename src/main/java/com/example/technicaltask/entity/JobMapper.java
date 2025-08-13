package com.example.technicaltask.entity;

import com.example.technicaltask.model.Job;
import com.example.technicaltask.model.Tag;
import com.example.technicaltask.utils.DateUtils;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JobMapper {

    public Job parseToJob(Document document, Job job) {

        Elements jobElements = document.select("div[data-testid=content]");
        job.setPositionName(jobElements.select("h2.sc-beqWaB.jqWDOR").text());
        job.setLogoUrl(jobElements.select("img").attr("src"));
        job.setOrganizationTitle(jobElements.select("div.sc-beqWaB.sc-gueYoa.kNPzSx.MYFxR").select("p").first().text());
        job.setOrganizationUrl(jobElements.select("a[data-testid=button-apply-now]").attr("href"));
        Elements laborAndLocation = jobElements.select("div.sc-beqWaB.bpXRKw");
        if (!laborAndLocation.isEmpty()) {
            job.setLaborFunction(laborAndLocation.get(0).text());
        } else {
            job.setLaborFunction(null);
        }
        if (laborAndLocation.size() > 1) {
            List<String> strings = parseLocations(laborAndLocation.get(1).text());
            job.setLocations(strings);
        }
        job.setPostedDate(jobElements.select("div.sc-beqWaB.cczurT").first() != null ? DateUtils.convertDateToUnixDate(jobElements.select("div.sc-beqWaB.cczurT").first().text()) : null);
        job.setDescriptionHtml(jobElements.select("div[data-testid=careerPage]").html());
        return job;
    }

    private List<String> parseLocations(String text) {
        String[] locations = text.split("·");

        return Arrays.stream(locations)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Job fromJson(JSONObject json) {
        Job job = new Job();

        // Припустимо, що в JSON є такі поля (підлаштуй під свій API):
        job.setJobUrl(json.optString("job_url")); // або "url", "jobUrl" — подивись в реальному JSON
        job.setPositionName(json.optString("position_name")); // заміни на реальні ключі
//        job.setCompanyName(json.optString("company_name"));
//        job.setLocation(json.optString("location"));
        job.setPostedDate(Long.parseLong(json.optString("posted_date"))); // або перетвори у LocalDate, якщо потрібно

        // Можеш додати інші поля, які потрібні з JSON, наприклад:
//        job.setDescription(json.optString("description"));

        // Якщо в JSON є масив тегів / функцій — мапимо їх у список Tag або JobFunctions
        if (json.has("tags")) {
            // Приклад: tags — масив рядків
            var tagsJsonArray = json.getJSONArray("tags");
            List<Tag> tags = new ArrayList<>();
            for (int i = 0; i < tagsJsonArray.length(); i++) {
                String tagName = tagsJsonArray.getString(i).trim();
                if (!tagName.isEmpty()) {
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    tags.add(tag);
                }
            }
            job.setTags(tags);
        }

        return job;
    }
}
